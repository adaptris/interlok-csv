# interlok-csv-schema

This depends on the [reference implementation](https://github.com/digital-preservation/csv-validator) which is written in scala and introduces a fair number of dependencies. For that reason this is provided as a separate artifact so you need to explictly make a choice as to whether you want to validate against a csv-schema.

The simplest possible configuration is something this:
```xml
<validate-csv-against-schema>
  <schema-file>/path/to/my/schema.csvs</schema-file>
</validate-csv-against-schema>
```

Behaviour around schema violations is configurable by selecting various `SchemaViolationHandler` implementations. The default implementation will be to throw a service exception if there are any formal errors (as opposed to warnings) in the CSV file.

Examples of CSV schemas can be here [https://github.com/digital-preservation/csv-validator/tree/master/csv-validator-core/src/test/resources/uk/gov/nationalarchives/csv/validator/acceptance](https://github.com/digital-preservation/csv-validator/tree/master/csv-validator-core/src/test/resources/uk/gov/nationalarchives/csv/validator/acceptance). 