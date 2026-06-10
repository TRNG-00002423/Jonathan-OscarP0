# 5. Create your own branch
git checkout -b name-feature

# 6. Make changes and commit
git add .
git commit -m "Added feature X"

# 7. Pull before pushing
git pull origin main
## If you're on your feature branch and want the latest main merged in:
git fetch origin
git merge origin/main

# 8. Push your branch
git push origin jonathan-feature

# 9. Create a Pull Request
On GitHub:
a. Open the repository
b. Click Compare & Pull Request
c. Create a PR from jonathan-feature → main