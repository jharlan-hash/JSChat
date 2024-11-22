#!/bin/bash

touch ./source
cat ./src/* > ./source
nvim -c "CopilotChat /COPILOT_REVIEW Review the four files concatenated together below (the files will be separated by comments): $(cat ./source)"
rm ./source
