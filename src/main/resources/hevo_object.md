# JSON Object Construction Guide

## Introduction

This document provides a comprehensive guide on how to construct a JSON object for configuring API interactions, data parsing, offset management, and other related functionalities. This guide is intended to be exhaustive, providing all necessary details to understand each component of the JSON configuration without referring to any codebase.

---

## Overview

The JSON object is designed to configure various aspects of API interactions, including:

- Defining which API to use.
- Specifying how to parse the API response.
- Managing offsets for data pagination.
- Configuring polling strategies.
- Setting applicable category types.

---

## Top-Level Fields

The JSON object contains the following top-level fields:

1. **api** (string, required)
2. **name** (string, required)
3. **namespace** (object, required)
4. **parse_config** (array, required)
5. **frequency_config** (object, optional, usually empty)
6. **polling_strategy** (array, optional)
7. **applicable_category_types** (array, required)

### api

- **Type**: String
- **Description**: Specifies which API tag to use for the object.
- **Required**: Yes

### name

- **Type**: String
- **Description**: The name of the object.
- **Required**: Yes

### namespace

- **Type**: Object
- **Description**: Defines the namespace for the object, which is displayed on the overview page.
- **Required**: Yes
- **Properties**:
    - **group_name** (string, optional): The group name of the namespace (e.g., database or schema name in a JDBC connector).
    - **object_name** (string, required): The object name of the namespace (e.g., table name in a JDBC connector).

### parse_config

- **Type**: Array of objects
- **Description**: Specifies how to parse events from the API response.
- **Required**: Yes
- **Items**:
    - **path** (string, required): The JSON path to the data to be parsed. Refer to [JsonPath](https://github.com/json-path/JsonPath) for syntax.
    - **event_name** (string, required): The name of the event.
    - **primary_keys** (array of strings, optional): List of fields that are primary key attributes.
    - **fields_to_exclude** (array of strings, optional): Fields to exclude from the parsed event.
    - **should_determine_per_record_offset** (boolean, optional): Indicates if this parse configuration should determine the per-record offset.

### frequency_config

- **Type**: Object
- **Description**: Configures the deferral period for the object after execution. Not usually required.
- **Required**: No
- **Properties**:
    - **defer_for** (number, optional): The defer duration in milliseconds. Usually empty if not needed. Even if present, not more that 6hr.

### polling_strategy

- **Type**: Array of objects
- **Description**: Defines strategies for managing data offsets and pagination.
- **Required**: No
- **Items**:
    - **category_type** (string, required): The category type the polling strategy applies to.
    - **offset_definition** (array of objects, optional): Defines initial offset fields.
    - **pre_poll** (array of offset field updates, optional): Updates to apply before polling.
    - **per_record_offset** (array of offset field updates, optional): Updates to apply per record.
    - **post_poll** (array of offset field updates, optional): Updates to apply after polling.
    - **has_more** (parameter, optional): Determines if more data is available.
    - **offset_display** (parameter, optional): Computes a value for displaying the offset.
    - **readable_offset_function** (parameter, optional): Generates a human-readable offset.
    - **edit_offset** (array of offset field updates, optional): Defines how to edit the offset.
    - **offset_editable** (boolean, optional): Indicates if the offset is editable.

### applicable_category_types

- **Type**: Array of strings
- **Description**: Lists the task category types applicable to this object.
- **Required**: Yes
- **Allowed Values**:
    - **"TABLE_OR_REPORT_TYPE"**
    - **"HISTORICAL_TABLE_TYPE"**

---

## Detailed Field Descriptions

### Namespace Object

The **namespace** field is an object that identifies the namespace of the data.

- **Properties**:
    - **group_name** (string, optional): The group name, such as a database or schema name.
    - **object_name** (string, required): The object name, such as a table name.

**Example**:

```json
"namespace": {
  "group_name": "public",
  "object_name": "users"
}
```

### Parse Configuration

The **parse_config** field is an array of parse configuration objects that specify how to extract events from the API response.

- **Properties**:
    - **path** (string, required): The JSON path to the data.
    - **event_name** (string, required): The name of the event to emit.
    - **primary_keys** (array of strings, optional): Fields that uniquely identify a record.
    - **fields_to_exclude** (array of strings, optional): Fields to exclude from the parsed data.
    - **should_determine_per_record_offset** (boolean, optional): Indicates if this parse configuration determines the per-record offset.

#### Parse Configuration Types

There are different types of parse configurations, but in this context, we focus on the default type.

- **Default Type**: `"JSON_BASED"`
- **Type Field**: If not specified, defaults to `"JSON_BASED"`.
- **Usage**: The default parse configuration parses JSON responses using the provided JSON path.

**Example**:

```json
{
  "path": "$.data.items[*]",
  "event_name": "ItemEvent",
  "primary_keys": ["id"],
  "fields_to_exclude": ["password"],
  "should_determine_per_record_offset": false
}
```

### Offset Management Rule

The **polling_strategy** field is an array of offset management rules that define how to manage offsets for pagination and data retrieval.

- **Properties**:
    - **category_type** (string, required): The category type this rule applies to.
    - **offset_definition** (array of offset definitions, optional): Defines the initial offset values.
    - **pre_poll** (array of offset field updates, optional): Updates to the offset before polling.
    - **per_record_offset** (array of offset field updates, optional): Updates to the offset per record.
    - **post_poll** (array of offset field updates, optional): Updates to the offset after polling.
    - **has_more** (parameter, optional): A parameter that evaluates to a boolean indicating if there is more data to fetch.
    - **offset_display** (parameter, optional): A parameter that computes a value for displaying the offset.
    - **readable_offset_function** (parameter, optional): A parameter that generates a human-readable representation of the offset.
    - **edit_offset** (array of offset field updates, optional): Defines how the offset can be edited.
    - **offset_editable** (boolean, optional): Indicates if the offset is editable.

#### Offset Definitions

Offset definitions initialize the offset fields used during data retrieval.

- **Properties**:
    - **id** (number, required): A unique identifier for the offset field.
    - **name** (string, required): The name of the offset field.
    - **value** (parameter, required): The initial value of the offset field.
    - **data_type** (string, required): The data type of the offset field.

**Example**:

```json
{
  "id": 0,
  "name": "page",
  "value": {
    "id": 0,
    "value": 1,
    "data_type": "LONG",
    "parameter_type": "CONST"
  },
  "data_type": "LONG"
}
```

#### Offset Field Updates

Offset field updates specify how to modify offset fields during different stages (pre-poll, per-record, post-poll, edit-offset).

- **Properties**:
    - **parameter** (parameter, required): The parameter that computes the new value.
    - **offset_field_name** (string, required): The name of the offset field to update.

**Example**:

```json
{
  "parameter": {
    "id": 0,
    "inputs": [
      {
        "id": 0,
        "value": "{{offset.page}}",
        "data_type": "LONG",
        "parameter_type": "PLACE_HOLDER"
      },
      {
        "id": 1,
        "value": 1,
        "data_type": "LONG",
        "parameter_type": "CONST"
      }
    ],
    "function": "add",
    "data_type": "LONG",
    "parameter_type": "FUNCTION"
  },
  "offset_field_name": "page"
}
```

#### Parameters

Parameters are used throughout the configuration to represent values or computations. They can be constants, placeholders, or functions.

- **Common Fields**:
    - **id** (number, required): A unique identifier for the parameter.
    - **name** (string, optional): A name for the parameter.
    - **data_type** (string, required): The data type of the parameter.
    - **ignore_if_null** (boolean, optional): Whether to ignore the parameter if its value is null.
    - **parameter_type** (string, required): The type of the parameter (`CONST`, `PLACE_HOLDER`, `FUNCTION`).

##### Parameter Types

1. **CONST**: A constant value.
    - **value** (required): The constant value.

   **Example**:

   ```json
   {
     "id": 0,
     "value": 100,
     "data_type": "LONG",
     "parameter_type": "CONST"
   }
   ```

2. **PLACE_HOLDER**: A placeholder that references a value from the context.
    - **value** (required): A string in the format `"{{context.variable}}"`. Where context is `offset` or `source_config`.

   **Example**:

   ```json
   {
     "id": 1,
     "value": "{{offset.page}}",
     "data_type": "LONG",
     "parameter_type": "PLACE_HOLDER"
   }
   ```

3. **FUNCTION**: A function that computes a value based on inputs.
    - **function** (required): The name of the function.
    - **inputs** (array of parameters, required): The inputs to the function.

   **Example**:

   ```json
   {
     "id": 2,
     "inputs": [
       {
         "id": 0,
         "value": "{{offset.page}}",
         "data_type": "LONG",
         "parameter_type": "PLACE_HOLDER"
       },
       {
         "id": 1,
         "value": 1,
         "data_type": "LONG",
         "parameter_type": "CONST"
       }
     ],
     "function": "add",
     "data_type": "LONG",
     "parameter_type": "FUNCTION"
   }
   ```

##### Data Types

- **LONG**: Represents integer values.
- **BOOLEAN**: Represents boolean values (`true` or `false`).
- **STRING**: Represents string values.

### Functions

Functions are used in parameters of type `FUNCTION` to compute values. Common functions include:

- **add**: Adds two numbers.
- **subtract**: Subtracts the second number from the first.
- **multiply**: Multiplies two numbers.
- **divide**: Divides the first number by the second.
- **current_ts_seconds**: Returns the current timestamp in seconds.
- **days_to_seconds**: Converts days to seconds.
- **format_ts_seconds**: Formats a timestamp in seconds into a string using a specified format.

---

## Example JSON Object

Below is a comprehensive example of the JSON object:

```json
{
  "api": "conversations",
  "name": "Conversations",
  "namespace": {
    "object_name": "conversations"
  },
  "parse_config": [
    {
      "path": "$",
      "event_name": "Conversations",
      "primary_keys": [
        "id"
      ],
      "should_determine_per_record_offset": true
    }
  ],
  "frequency_config": {},
  "polling_strategy": [
    {
      "category_type": "HISTORICAL_TABLE_TYPE",
      "offset_definition": [
        {
          "id": 0,
          "name": "updated_before",
          "value": {
            "id": 0,
            "function": "current_ts_seconds",
            "data_type": "LONG",
            "parameter_type": "FUNCTION"
          },
          "data_type": "LONG"
        },
        {
          "id": 1,
          "name": "updated_after",
          "value": {
            "id": 0,
            "function": "current_ts_seconds",
            "data_type": "LONG",
            "parameter_type": "FUNCTION"
          },
          "data_type": "LONG"
        },
        {
          "id": 2,
          "name": "has_record",
          "value": {
            "id": 0,
            "value": false,
            "data_type": "BOOLEAN",
            "parameter_type": "CONST"
          },
          "data_type": "BOOLEAN"
        }
      ],
      "pre_poll": [
        {
          "parameter": {
            "id": 0,
            "name": "updated_before",
            "value": "{{offset.updated_after}}",
            "data_type": "LONG",
            "ignore_if_null": true,
            "parameter_type": "PLACE_HOLDER"
          },
          "offset_field_name": "updated_before"
        },
        {
          "parameter": {
            "id": 1,
            "name": "has_record",
            "value": false,
            "data_type": "BOOLEAN",
            "ignore_if_null": true,
            "parameter_type": "CONST"
          },
          "offset_field_name": "has_record"
        },
        {
          "parameter": {
            "id": 2,
            "name": "updated_after",
            "inputs": [
              {
                "id": 0,
                "name": "updated_after",
                "value": "{{offset.updated_after}}",
                "data_type": "LONG",
                "ignore_if_null": true,
                "parameter_type": "PLACE_HOLDER"
              },
              {
                "id": 1,
                "inputs": [
                  {
                    "id": 0,
                    "value": 30,
                    "data_type": "LONG",
                    "parameter_type": "CONST"
                  }
                ],
                "function": "days_to_seconds",
                "data_type": "LONG",
                "parameter_type": "FUNCTION"
              }
            ],
            "function": "subtract",
            "data_type": "LONG",
            "parameter_type": "FUNCTION"
          },
          "offset_field_name": "updated_after"
        }
      ],
      "per_record_offset": [
        {
          "parameter": {
            "id": 0,
            "value": true,
            "data_type": "BOOLEAN",
            "parameter_type": "CONST"
          },
          "offset_field_name": "has_record"
        }
      ],
      "has_more": {
        "id": 0,
        "value": "{{offset.has_record}}",
        "data_type": "BOOLEAN",
        "parameter_type": "PLACE_HOLDER"
      }
    },
    {
      "category_type": "TABLE_OR_REPORT_TYPE",
      "offset_definition": [
        {
          "id": 0,
          "name": "updated_before",
          "value": {
            "id": 0,
            "function": "current_ts_seconds",
            "data_type": "LONG",
            "parameter_type": "FUNCTION"
          },
          "data_type": "LONG"
        },
        {
          "id": 1,
          "name": "updated_after",
          "value": {
            "id": 0,
            "function": "current_ts_seconds",
            "data_type": "LONG",
            "parameter_type": "FUNCTION"
          },
          "data_type": "LONG"
        }
      ],
      "pre_poll": [
        {
          "parameter": {
            "id": 0,
            "name": "updated_before",
            "function": "current_ts_seconds",
            "data_type": "LONG",
            "parameter_type": "FUNCTION"
          },
          "offset_field_name": "updated_before"
        }
      ],
      "post_poll": [
        {
          "parameter": {
            "id": 0,
            "name": "updated_after",
            "function": "current_ts_seconds",
            "data_type": "LONG",
            "parameter_type": "FUNCTION"
          },
          "offset_field_name": "updated_after"
        }
      ],
      "edit_offset": [
        {
          "parameter": {
            "id": 0,
            "inputs": [
              {
                "id": 0,
                "value": "{{new_offset}}",
                "data_type": "LONG",
                "ignore_if_null": false,
                "parameter_type": "PLACE_HOLDER"
              },
              {
                "id": 1,
                "value": 1000,
                "data_type": "LONG",
                "parameter_type": "CONST"
              }
            ],
            "function": "divide",
            "data_type": "LONG",
            "parameter_type": "FUNCTION"
          },
          "offset_field_name": "updated_after"
        }
      ],
      "offset_display": {
        "id": 0,
        "inputs": [
          {
            "id": 0,
            "value": "{{offset.updated_after}}",
            "data_type": "LONG",
            "ignore_if_null": false,
            "parameter_type": "PLACE_HOLDER"
          },
          {
            "id": 1,
            "value": 1000,
            "data_type": "LONG",
            "parameter_type": "CONST"
          }
        ],
        "function": "multiply",
        "data_type": "LONG",
        "parameter_type": "FUNCTION"
      },
      "readable_offset_function": {
        "id": 0,
        "inputs": [
          {
            "id": 0,
            "value": "{{offset.updated_after}}",
            "data_type": "LONG",
            "parameter_type": "PLACE_HOLDER"
          },
          {
            "id": 1,
            "value": "yyyy-MM-dd HH:mm:ss",
            "data_type": "STRING",
            "parameter_type": "CONST"
          }
        ],
        "function": "format_ts_seconds",
        "data_type": "STRING",
        "parameter_type": "FUNCTION"
      },
      "offset_editable": true
    }
  ],
  "applicable_category_types": [
    "HISTORICAL_TABLE_TYPE",
    "TABLE_OR_REPORT_TYPE"
  ]
}
```

---

## Constructing the JSON Object Step-by-Step

1. **Define the API and Object Name**:
    - Set the **api** field to the API tag you wish to use.
    - Set the **name** field to a descriptive name for the object.

2. **Configure the Namespace**:
    - Create a **namespace** object.
    - Specify the **object_name** (required) and **group_name** (optional).

3. **Define the Parse Configurations**:
    - Create a **parse_config** array.
    - For each event to parse, add an object with:
        - **path**: The JSON path to the data.
        - **event_name**: The name of the event.
        - **primary_keys**: List of primary key fields (if applicable).
        - **fields_to_exclude**: Fields to exclude (if any).
        - **should_determine_per_record_offset**: Set to `true` if this configuration determines the per-record offset.

4. **Configure Frequency (Optional)**:
    - If deferral is needed, add a **frequency_config** object with **defer_for** set to the required duration in milliseconds.

5. **Define Polling Strategies**:
    - Create a **polling_strategy** array.
    - For each strategy:
        - Specify the **category_type**.
        - Define **offset_definition** to initialize offset fields.
        - Add **pre_poll**, **per_record_offset**, **post_poll**, **edit_offset** as needed, using offset field updates.
        - Include **has_more**, **offset_display**, **readable_offset_function**, and **offset_editable** as required.

6. **Specify Applicable Category Types**:
    - List the category types in **applicable_category_types**.

---

## Validation Rules and Best Practices

- **Required Fields**: Ensure all required fields are included.
- **Unique IDs**: All parameters and offset definitions should have unique IDs within their context.
- **Data Types**: Match the **data_type** of parameters and offset fields correctly.
- **Parameter Types**: Use the correct **parameter_type** (`CONST`, `PLACE_HOLDER`, or `FUNCTION`).
- **Functions**: Ensure that any functions used are defined and accept the provided inputs.
- **Offset Fields**: Offset field updates must reference fields defined in **offset_definition**.
- **Placeholders**: Use the correct placeholder syntax `"{{context.variable}}"`.
- **Consistency**: Maintain consistent naming and structuring throughout the configuration.

---

## Conclusion

This guide provides a detailed explanation of constructing the JSON object for configuring API interactions, data parsing, and offset management. By following the step-by-step instructions and adhering to the validation rules, you can create a valid and functional configuration without referring to any codebase.

Remember to:

- Carefully define each component and its properties.
- Validate your configuration against the described rules.
- Use the example as a reference for structuring your own configurations.

---

**Note**: This document is exhaustive and includes all necessary details to understand and construct the JSON object without any reference to external code or omitted information.