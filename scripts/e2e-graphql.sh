#!/usr/bin/env bash
set -u
G=http://localhost:8080/graphql
pass=0; fail=0
chk() { if echo "$3" | grep -q "$2"; then echo "PASS: $1"; pass=$((pass+1)); else echo "FAIL: $1"; echo "   want ~[$2]"; echo "   got: $3"; fail=$((fail+1)); fi; }
gql() { # query, token(optional)
  local q="$1"; local tok="${2:-}"
  local auth=(); [ -n "$tok" ] && auth=(-H "Authorization: Token $tok")
  curl -s "$G" -H 'Content-Type: application/json' "${auth[@]}" -d "$(python3 -c 'import json,sys;print(json.dumps({"query":sys.argv[1]}))' "$q")"
}

# register + get token via REST (login mutation also tested below)
REST=http://localhost:8080
TOKEN=$(curl -s -X POST $REST/users -H 'Content-Type: application/json' -d '{"user":{"email":"gql@example.com","username":"gqluser","password":"password123"}}' | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
[ -z "$TOKEN" ] && TOKEN=$(curl -s -X POST $REST/users/login -H 'Content-Type: application/json' -d '{"user":{"email":"gql@example.com","password":"password123"}}' | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
echo "TOKEN=${TOKEN:0:16}..."

echo "== login mutation =="
R=$(gql 'mutation { login(email:"gql@example.com", password:"password123"){ user { username email token } } }')
chk "login returns user" '"username":"gqluser"' "$R"

echo "== me query =="
R=$(gql '{ me { username email profile { username following } } }' "$TOKEN")
chk "me returns current user" '"username":"gqluser"' "$R"

echo "== tags query =="
R=$(gql '{ tags }')
chk "tags list returned" '"tags"' "$R"

echo "== createArticle mutation =="
R=$(gql 'mutation { createArticle(input:{title:"GQL Article", description:"d", body:"b", tagList:["graphql","x"]}){ article { slug title favoritesCount author { username } } } }' "$TOKEN")
echo "$R"
chk "createArticle returns article" '"title":"GQL Article"' "$R"
SLUG=$(echo "$R" | sed -n 's/.*"slug":"\([^"]*\)".*/\1/p')
echo "SLUG=$SLUG"

echo "== article(slug) query =="
R=$(gql "{ article(slug:\"$SLUG\"){ slug title author { username } comments(first:10){ edges { node { body } } } } }" "$TOKEN")
chk "article by slug" "\"slug\":\"$SLUG\"" "$R"

echo "== articles connection query =="
R=$(gql '{ articles(first:5){ edges { cursor node { slug } } pageInfo { hasNextPage } } }')
chk "articles connection" '"edges"' "$R"

echo "== favoriteArticle mutation =="
R=$(gql "mutation { favoriteArticle(slug:\"$SLUG\"){ article { favorited favoritesCount } } }" "$TOKEN")
chk "favorited true" '"favorited":true' "$R"
chk "favoritesCount 1" '"favoritesCount":1' "$R"

echo "== unfavoriteArticle mutation =="
R=$(gql "mutation { unfavoriteArticle(slug:\"$SLUG\"){ article { favorited favoritesCount } } }" "$TOKEN")
chk "favorited false" '"favorited":false' "$R"

echo "== addComment mutation =="
R=$(gql "mutation { addComment(slug:\"$SLUG\", body:\"gql comment\"){ comment { id body author { username } } } }" "$TOKEN")
echo "$R"
chk "comment added" '"body":"gql comment"' "$R"
CID=$(echo "$R" | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')

echo "== article.comments after add =="
R=$(gql "{ article(slug:\"$SLUG\"){ comments(first:10){ edges { node { body } } } } }" "$TOKEN")
chk "comment visible in connection" 'gql comment' "$R"

echo "== deleteComment mutation =="
R=$(gql "mutation { deleteComment(slug:\"$SLUG\", id:\"$CID\"){ success } }" "$TOKEN")
chk "deleteComment success" '"success":true' "$R"

echo "== profile query =="
R=$(gql '{ profile(username:"gqluser"){ profile { username following } } }')
chk "profile query" '"username":"gqluser"' "$R"

echo "== followUser mutation =="
R=$(gql 'mutation { followUser(username:"janedoe"){ profile { username following } } }' "$TOKEN")
chk "follow following=true" '"following":true' "$R"
R=$(gql 'mutation { unfollowUser(username:"janedoe"){ profile { following } } }' "$TOKEN")
chk "unfollow following=false" '"following":false' "$R"

echo "== createUser mutation (UserResult union) =="
R=$(gql 'mutation { createUser(input:{email:"gql2@example.com", username:"gqluser2", password:"password123"}){ ... on UserPayload { user { username } } ... on Error { message } } }')
chk "createUser union UserPayload" '"username":"gqluser2"' "$R"

echo "== updateUser mutation =="
R=$(gql 'mutation { updateUser(changes:{bio:"gql bio"}){ user { username } } }' "$TOKEN")
chk "updateUser ok" '"username":"gqluser"' "$R"

echo "== deleteArticle (cleanup) =="
R=$(gql "mutation { deleteArticle(slug:\"$SLUG\"){ success } }" "$TOKEN")
chk "deleteArticle success" '"success":true' "$R"

echo ""
echo "GQL RESULT: pass=$pass fail=$fail"
