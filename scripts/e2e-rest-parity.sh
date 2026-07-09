#!/usr/bin/env bash
# End-to-end REST parity smoke against the gateway (port 8080).
set -u
G=http://localhost:8080
pass=0; fail=0
chk() { # desc, expected_substr, actual
  if echo "$3" | grep -q "$2"; then echo "PASS: $1"; pass=$((pass+1)); else echo "FAIL: $1"; echo "   expected ~ [$2]"; echo "   got: $3"; fail=$((fail+1)); fi
}

echo "== login seed user johndoe =="
LOGIN=$(curl -s -X POST $G/users/login -H 'Content-Type: application/json' -d '{"user":{"email":"john@example.com","password":"password"}}')
echo "$LOGIN"
TOKEN=$(echo "$LOGIN" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
if [ -z "$TOKEN" ]; then
  echo "-- seed login failed, registering a fresh user --"
  REG=$(curl -s -X POST $G/users -H 'Content-Type: application/json' -d '{"user":{"email":"e2e@example.com","username":"e2euser","password":"password123"}}')
  echo "$REG"
  TOKEN=$(echo "$REG" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
fi
echo "TOKEN=${TOKEN:0:20}..."
A="Authorization: Token $TOKEN"

echo "== GET /user (current user) =="
CU=$(curl -s $G/user -H "$A"); chk "current user has email" '"email"' "$CU"

echo "== GET /tags =="
TAGS=$(curl -s $G/tags); chk "tags payload" '"tags"' "$TAGS"

echo "== GET /articles =="
ARTS=$(curl -s $G/articles); chk "articles list" '"articlesCount"' "$ARTS"

echo "== POST /articles (create) =="
CREATE=$(curl -s -X POST $G/articles -H "$A" -H 'Content-Type: application/json' -d '{"article":{"title":"Gateway E2E Test","description":"desc","body":"hello body","tagList":["e2e","gw"]}}')
echo "$CREATE"
chk "created article returns author profile" '"author"' "$CREATE"
chk "created article favoritesCount present" '"favoritesCount"' "$CREATE"
SLUG=$(echo "$CREATE" | sed -n 's/.*"slug":"\([^"]*\)".*/\1/p')
echo "SLUG=$SLUG"

echo "== GET /articles/{slug} =="
GETA=$(curl -s $G/articles/$SLUG); chk "get by slug" "\"slug\":\"$SLUG\"" "$GETA"

echo "== POST /articles/{slug}/favorite (recompose) =="
FAV=$(curl -s -X POST $G/articles/$SLUG/favorite -H "$A")
echo "$FAV"
chk "favorite recomposed ArticleData" '"article"' "$FAV"
chk "favorited=true after favorite" '"favorited":true' "$FAV"
chk "favoritesCount=1 after favorite" '"favoritesCount":1' "$FAV"

echo "== DELETE /articles/{slug}/favorite =="
UNFAV=$(curl -s -X DELETE $G/articles/$SLUG/favorite -H "$A")
echo "$UNFAV"
chk "favorited=false after unfavorite" '"favorited":false' "$UNFAV"

echo "== POST comment =="
CMT=$(curl -s -X POST $G/articles/$SLUG/comments -H "$A" -H 'Content-Type: application/json' -d '{"comment":{"body":"nice article"}}')
echo "$CMT"
chk "comment created with author" '"author"' "$CMT"
CID=$(echo "$CMT" | sed -n 's/.*"comment":{"id":\([0-9]*\).*/\1/p')
echo "CID=$CID"

echo "== GET comments =="
CMTS=$(curl -s $G/articles/$SLUG/comments -H "$A"); chk "comments list" '"comments"' "$CMTS"

echo "== DELETE comment =="
DC=$(curl -s -o /dev/null -w '%{http_code}' -X DELETE $G/articles/$SLUG/comments/$CID -H "$A"); chk "delete comment 200/204" '20' "$DC"

echo "== GET /profiles/{username} =="
PROF=$(curl -s $G/profiles/johndoe -H "$A"); chk "profile payload" '"profile"' "$PROF"

echo "== follow / unfollow =="
FOL=$(curl -s -X POST $G/profiles/janedoe/follow -H "$A"); chk "following=true" '"following":true' "$FOL"
UNFOL=$(curl -s -X DELETE $G/profiles/janedoe/follow -H "$A"); chk "following=false" '"following":false' "$UNFOL"

echo "== GET /articles/feed =="
FEED=$(curl -s $G/articles/feed -H "$A"); chk "feed list" '"articlesCount"' "$FEED"

echo "== DELETE article (cleanup, author-only) =="
DA=$(curl -s -o /dev/null -w '%{http_code}' -X DELETE $G/articles/$SLUG -H "$A"); chk "delete article 200/204" '20' "$DA"

echo ""
echo "RESULT: pass=$pass fail=$fail"
