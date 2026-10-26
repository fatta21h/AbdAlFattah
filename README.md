name: Build Android APK

on: workflow_dispatch

jobs:
build:
runs-on: ubuntu-latest
steps:
- name: Checkout repository
uses: actions/checkout@v4

  - name: Check project files
    run: |
      echo "Checking repository files"
      ls -la
      find . -maxdepth 3 -type f | head -100
