#!/bin/bash

# Default values for testing files
relative_path_test_directory=$'test/auto/'
input_file_suffix=$'-input.bc'
output_file_suffix=$'-output.txt'

# The will output if the files failed or passed
diffFiles() {
	if diff <(java -cp target/SimpleBC-1.0-jar-with-dependencies.jar com.codecaptured.SimpleBC.SimpleBC $1) $2 > /dev/null
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

