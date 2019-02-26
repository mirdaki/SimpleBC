#!/bin/bash

# TODO
# Error checking to make sure test files both exist
# Error checking to make sure the grammar complies and ANTLR4 is in the class path

# NOTE: The class path must be valid to work. If it isn't already set, please update it in this file before running
# Add the default ANTLR4 location to the class path just in case
export CLASSPATH=".:/usr/local/lib/antlr-4.7.1-complete.jar:$CLASSPATH"

# Default values for testing files
relative_path_test_directory=$'../test/'
input_file_suffix=$'-input.bc'
output_file_suffix=$'-output.txt'

# Reference (non-functioning) code for running and compiling the code
# antlr4=(java -Xmx500M -cp "/usr/local/lib/antlr-4.7.1-complete.jar:$CLASSPATH" org.antlr.v4.Tool)
# grun=java -Xmx500M -cp "/usr/local/lib/antlr-4.7.1-complete.jar:$CLASSPATH" org.antlr.v4.gui.TestRig
# grun="$(java -Xmx500M -cp $CLASSPATH org.antlr.v4.gui.TestRig)"

# The will output if the files failed or passed
diffFiles() {
	if diff <(java -Xmx500M -cp $CLASSPATH org.antlr.v4.gui.TestRig SimpleBC exprList $1) $2 > /dev/null
	then
			echo "Pass: " $1
	else
			echo "Fail: " $1
	fi
}

# Go through all the files in the test directory that match the input suffix
for file in $relative_path_test_directory*$input_file_suffix
do
	# Compare the input files output with the output file
	diffFiles $file ${file%$input_file_suffix}$output_file_suffix
done

