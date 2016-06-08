# FleX2Json
## Brief
FleX2Json is a solution to organize your data with Microsoft Excel and to convert them into JSON/XML files, with a very configable, expandable and  flexible approach. 

There are many projects providing format conversion functions already but we found that most of them are not powerful or flexible enough to meet complex requirements. So we developed FleX2Json and continually improved it along with our own game projects. Now we decide to open source it to benefit the engineers who need. 

## Features

* **Java** based. Run it anywhere.
* Run **single command** to generate JSON/XML files from multiple sheets from multiple .xls/.xlsx sources.
* Can **custom the output paths and extensions** for every source file or even every sheet by modifying config.xml.
* **Auto-check data types** for every attribute if is needed.
* Flexibly insert human-readable comment in the excel sheets for every object and attribute.
* Support 3-layers JSON structure. However in current version, only the array of basic types is supported in the 3rd layer.
* Potential to support unlimited-layers JSON structure in further updates.

## How to Use
### Quick use of FleX2Json
 Generate sample JSON files with 3 simple steps:
 
1. Update your Java to the latest version from [HERE](https://java.com/en/download/).
2. Download latest release [FleX2Json.zip](https://github.com/geekmouse/FleX2Json/releases) to any directory (eg. `YourPath/`) 
3. In Terminal or any commandline tools, redirect to `YourPath/FleX2Json`, run `java -jar FleX2Json.jar`. Work done. The JSON files generated from **SampleData.xls** and **SampleGame.xls** can be found under `YourPath/output`. 

There are detailed instructions of each field in`SampleData.xls::SimpleTest` and `config.xml`. Edit the values and repeat step 3 to compare the results, and you'll quickly catch how to use it.

For complete introduction of how to use FleX2Json, please continue reading.

### Usage of parametrizing mode
When running without any parameter, FleX2Json will read configurations from `config.xml`. Otherwise, you can use 
**parametrizing mode** to convert single excel sheet by setting parameters in command lines. The following parameters are supported above v0.92 and you can also run `java -jar FleX2Json.jar -h` for more details.

**Table 0:** Parameters in parametrizing mode

Parameter | Meaning | Required? 	|Valid values | Default Value
----		|----		|---			|----	|---
i	|Input file path|Required|Valid path|N/A
s	|Input sheet name|Required|Valid sheet name| N/A
o	|Output path|Required|Valid path|N/A
f	|Output file format|Optional|xml or json. Case insensitive|json
x	|Output file extension|Optional|Any string you need |if f==xml, x='.xml', else x='.json'.
h	|Show help list|Optional


### Key files of FleX2Json

**Table 1:** File list and functions of the standalone FleX2Json

File name | usage 
----|-----
FleX2Json.jar| Core executable file
config.xml| Config the paths of input/output files and the output extensions. 
SampleData.xls/SampleGame.xls| Sample source files
output/| Directory for output files. Can be changed in `config.xml`
 
### The Excel data source structure
* Both the format Excel 97-2004 (.xls) and Excel 2007 above (.xlsx) are supported in current version.
* One sheet is converted into one JSON file. The JSON file name is the same with the sheet name. In future versions, it'll allow merging data from different excel sheets with different relationships into one single JSON file.
* Each sheet is considered to be an array of objects whose attributes are defined in columns.
* The first 3 rows in every column are reserved for necessary information of the attribute. See `Table 2` below.

Table 2: Functions and availabilities of reserved rows

Row index   | Function | Required?
-------| ---------- |------
 1| Human-readable description of the attribute in this column. | Optional.
 2| The JSON key of this attribute. | Required. Except it's a comment column.
 3| The data type of this attribute. Before converting, the value type will be checked. | Optional. If empty, the attribute is treated as "String". Refer to `Table 2` for the syntax of data types.


* The syntax of data types.

Symbol | Meaning | Supported
---|----|----
b| Boolean |Yes
i| Integer| Yes
f| Float| Yes
s or `Empty`| String| Yes
B| Array of Booleans| Yes
I or a| Array of Integers| Yes
F| Array of Float| Yes
S or A| Array of Strings| Yes
o{`sheet_name`}| Object read from sheet with name `sheet_name` | Future version
o{`file_name`:`sheet_name`} |Object read from `sheet_name` from file `file_name` | Future version
O| Object array| Future version
N | Not to convert. Use for temporarily hide the attributes. Won't be converted into JSON.| Yes
C| Human-readable comment. Won't be converted into JSON. |Yes

### Configure `config.xml`
Open `config.xml`. There are detailed instructions of how to edit this file.
