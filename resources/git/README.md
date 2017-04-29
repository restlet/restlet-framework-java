Install the commit hook
```bash
cd <WORKSPACE_DIR>
ln -s ../../resources/git/hooks/validate-commit-msg.js .git/hooks/commit-msg
```

Install the commit template

```bash
cd <WORKSPACE_DIR>
git config commit.template resources/git/commit.template.txt
```
