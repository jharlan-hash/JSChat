#!/bin/bash

touch ./source
cat ./src/* > ./source
nvim -c "CopilotChat /COPILOT_REVIEW Review the three files concatenated together below: $(cat ./source)"
rm ./source
