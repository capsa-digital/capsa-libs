from os import listdir, rename, remove
from os.path import isfile, join
import fileinput
import argparse
import re

"""
    Example of use (in command line):
        python refactor_files_models.py --package package.name.com

    eample of package: com.telus.ews.poq.api.gateway.am.model
"""


old_package_name = "io.swagger.server.models"

def remove_duplicated_lines(input_file):
	outputfile_name = f"{input_file}_2"
	lines_seen = set()
	outfile = open(outputfile_name, "w")
	for line in open(input_file, "r"):
		modified_line = line.replace(",", "")
		if modified_line not in lines_seen or "/**" in line or "*/" in line: # not a duplicate
			outfile.write(line)
			lines_seen.add(modified_line)
	outfile.close()
	
	remove(input_file)
	rename(outputfile_name, input_file)

def is_enum_file(filename):
	content = None
	with open(filename, "r") as f:
		content = f.readlines()


	class_name = filename.split(".")[0]
	line_name = f"enum class {class_name}"

	content = [x.strip() for x in content]
	for c in content:
		if line_name in c:
			return True

	return False

def read_files(mypath="./"):
	return [f for f in listdir(mypath) if isfile(join(mypath, f))]

def rename2dto(old_filenames, package_name):
	new_filenames = []
	class_names = []

	print("filename -> filename + Dto")
	for file in old_filenames:
		if "py" not in file:
			# remove the duplicated lines
			remove_duplicated_lines(file)
			if is_enum_file(file):
				for line in fileinput.input(file, inplace=1):
					if old_package_name in line:
						line = line.replace(old_package_name, package_name)
					print(line, end='')
				continue

			class_name = file.split('.')[0]
			class_names.append(class_name)
			if "Dto" not in class_name:
				new_filename = f"{class_name}Dto.kt"
				rename(file, new_filename)
				new_filenames.append(new_filename)
				# print(f"{file} -> {new_filename}")
		else:
			print("REMOVE THE SCRIPT")
			# remove(file)
	return new_filenames, class_names


def find_replace_old_classes_names(new_filenames, class_names, package_name=""):
	for file, cn in zip(new_filenames, class_names):
		for line in fileinput.input(file, inplace=1):
			if "data class" in line and "internal" not in line:
				line = f"@JsonRootName(value = \"{cn}\")\ninternal data class {cn}Dto ("
				print(line)
			elif "package" in line:
				line = f"@file:Suppress(\"MaxLineLength\")\npackage {package_name}\n\nimport com.fasterxml.jackson.annotation.JsonRootName\nimport com.fasterxml.jackson.annotation.JsonProperty"
				print(line)
			elif old_package_name in line:
				line = line.replace(old_package_name, package_name)
				possible_class = []
				for old_cn in class_names:
					if old_cn in line:
						if "Dto" not in line:
							possible_class.append(old_cn)
				
				if possible_class:
					max_score_cn = max(possible_class, key=len)
					if max_score_cn == line.split(".")[-1].strip():
						line = line.replace(max_score_cn, f"{max_score_cn}Dto")
					
				print(line, end='')
# 			elif "val @baseType" in line or "val @schemaLocation" in line or "val @type" in line or "val @referredType" in line:
			elif "val @" in line:
				value_type = line.split("@")[1].split(":")[0]
				line = line.replace("@", "")
				line = f"\t@get:JsonProperty(\"@{value_type}\")\n{line}"
				print(line, end='')
			else:
				# replace the class name in files for params type
				possible_class = []
				for old_cn in class_names:
					if old_cn in line:
						if "enum" not in line:
							possible_class.append(old_cn)

				if possible_class:
					max_score_cn = max(possible_class, key=len)
					words = re.split("\W+", line)
					# check if is the right class
					for w in words:
						if w == max_score_cn:
							line = line.replace(max_score_cn, f"{max_score_cn}Dto")
							break

				print(line, end='')


if __name__ == '__main__':
	pyparser = argparse.ArgumentParser(description='Modify name of classes, also find and replace in files')
	pyparser.add_argument("--path", action="store", default="./", type=str, help="The path where is all kotlin files which need to be modified")
	pyparser.add_argument("--package", action="store", type=str, help="The package name which need to be replaced in files", required=True)

	my_args = pyparser.parse_args()
	

	old_filenames = read_files(my_args.path)
	new_filenames, class_names = rename2dto(old_filenames, my_args.package)
	find_replace_old_classes_names(new_filenames, class_names, my_args.package)

	print("Successfuly modified files")