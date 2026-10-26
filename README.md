name: Build Android APK

on:
workflow_dispatch:
push:
branches:
- main

jobs:
build:
runs-on: ubuntu-latest
steps:
- name: Checkout repository
uses: actions/checkout@v4

  - name: Set up JDK
    uses: actions/setup-java@v4
    with:
      distribution: temurin
      java-version: '17'

  - name: Check project files
    run: |
      echo "Checking project files"
      ls -la
      find . -maxdepth 3 -type f | head -100
