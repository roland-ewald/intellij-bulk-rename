## IntelliJ plugin to bulk-rename Java types

This IntelliJ plugin allows to batch-refactor Java types.

### Input CSV format

[Sample file](/src/test/resources/sample-refactoring.csv) has this format:

- absolute path to file
- new type name (of public type)
- flag: search in comments?
- flag: search in other text?

```csv
File Name, New Type Name,Search in Comments, Search Text Occurrences
/absolute/path/to/file/Sample.java,Sample2,true,true
```

### Usage notes

The usage is still interactive, as you will be asked e.g. whether parameters and tests should be renamed as well. 