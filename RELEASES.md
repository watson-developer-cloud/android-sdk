# Release process

We use [bumpversion] to update the project version, commit, and tag the changes.

### 1. Updating the version
A small command line tool to simplify releasing software by updating all version strings in your source code by the correct increment.  
Install it with:
```bash
pip install bumpversion
```

### 2. Doing a release

```
bumpversion major|minor|patch
git push origin master
git push origin --tags
```

`bumpversion *` will update the version field in the appropriate places, create a git commit and tag for the version, and publish the tag to GitHub.  
`git push origin master` will publish the changes to the remote branch. 
`git push origin --tags` will deploy the tagged release to GitHub and Bintray.

[bumpversion]: https://pypi.python.org/pypi/bumpversion