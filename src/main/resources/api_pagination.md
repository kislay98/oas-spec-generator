# API Configuration Guide

## Introduction

This document provides a comprehensive guide on how to construct the `apis` part of the JSON configuration for implementing APIs and pagination. It explains the structure, required fields, parameter types, data types, and includes examples to help you understand how to define your API configurations without referring to the underlying code.

## APIs Array Structure

The `apis` field is an array containing one or more API definitions. Each API definition specifies details such as the URL, HTTP method, headers, path parameters, query parameters, and variables.

### JSON Schema Overview

```json
{
  "type": "object",
  "required": ["apis"],
  "properties": {
    "apis": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["url", "method"],
        "properties": {
          "tag": {
            "type": "string",
            "description": "The tag for the API, referenced in the Connector Template API component."
          },
          "url": {
            "type": "string",
            "description": "The URL string, can contain placeholders."
          },
          "method": {
            "type": "string",
            "description": "The HTTP method."
          },
          "headers": {
            "type": "array",
            "items": { /* See Parameter Definition */ }
          },
          "path_params": {
            "type": "array",
            "items": { /* See Parameter Definition */ }
          },
          "query_params": {
            "type": "array",
            "items": { /* See Parameter Definition */ }
          },
          "variables": {
            "type": "array",
            "items": {
              "type": "object",
              "properties": {
                "param_name": {
                  "type": "string"
                },
                "param_value": {
                  "type": "string"
                }
              }
            }
          }
        }
      },
      "minItems": 1
    }
  },
  "description": "Configuration to implement API and paginate"
}
```

## API Object Structure

Each API object in the `apis` array should contain the following properties:

- **tag** (optional): A string identifier for the API, used for referencing in the connector template.
- **url** (required): The URL for the API endpoint. It can include placeholders for dynamic values.
- **method** (required): The HTTP method to use (e.g., `"GET"`, `"POST"`).
- **headers** (optional): Includes header parameters using **AbstractParameter**s. Never includes Authorization here
- **path_params** (optional): An array of path parameters (see [Parameter Definition](#parameter-definition)).
- **query_params** (optional): An array of query parameters (see [Parameter Definition](#parameter-definition)).
- **variables** (optional): An array of variables used within the API configuration.

## Parameter Definition

Parameters in `headers`, `path_params`, and `query_params` are defined using an **AbstractParameter** object. Each parameter must include the `parameter_type` field, which determines the type of parameter and additional required fields.

### Common Fields

All parameter objects, regardless of type, share the following fields:

- **id** (required): A unique integer identifier for the parameter.
- **name** (optional): The name of the parameter (e.g., the name of the header or query parameter).
- **data_type** (required): The data type of the parameter's value. Possible values are:
    - `"LONG"`
    - `"STRING"`
    - `"BOOLEAN"`
    - `"RECORD"`
    - `"LIST"`
- **ignore_if_null** (optional): A boolean indicating whether to ignore the parameter if its value is `null`. Default is `false`.
- **parameter_type** (required): The type of the parameter. Possible values are:
    - `"CONST"`
    - `"PLACE_HOLDER"`
    - `"FUNCTION"`

### Parameter Types

#### 1. Constant Parameter (`"CONST"`)

A constant parameter holds a fixed value.

**Required Fields:**

- **value** (required): The constant value of the parameter. The type must match the specified `data_type`.

**Example:**

```json
{
  "id": 0,
  "name": "limit",
  "value": 100,
  "data_type": "LONG",
  "parameter_type": "CONST"
}
```

#### 2. Placeholder Parameter (`"PLACE_HOLDER"`)

A placeholder parameter references a value from the configuration context, such as user-provided inputs or environment variables.

**Required Fields:**

- **value** (required): A string in the format `"{{source_config.param}}"`, indicating where to retrieve the value from.

**Value Format Details:**

- Must start with `"{{"` and end with `"}}"`.
- If the value contains a dot (`"."`), it separates the configuration (`config`) and the parameter (`param`).
    - `"{{source_config.param}}"`: References a parameter `param` within a configuration `source_config`.

**Example:**

```json
{
  "id": 1,
  "name": "api_key",
  "value": "{{source_config.api_key}}",
  "data_type": "STRING",
  "parameter_type": "PLACE_HOLDER"
}
```

#### 3. Function Parameter (`"FUNCTION"`)

A function parameter computes its value by applying a function to one or more input parameters.

**Required Fields:**

- **function** (required): The name of the function to be applied (e.g., `"multiply"`).
- **inputs** (required): An array of input parameters. Each input is an AbstractParameter object and can be of any parameter type (`CONST`, `PLACE_HOLDER`, or `FUNCTION`).

**Function Details:**

- The function specified in the `function` field must be defined in the execution context and capable of processing the provided inputs.
- Inputs are processed in order based on their `id`.

**Example:**

```json
{
  "id": 2,
  "name": "timestamp",
  "inputs": [
    {
      "id": 0,
      "value": "{{offset.last_updated}}",
      "data_type": "LONG",
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
}
```

In this example, the function `"multiply"` multiplies the value from `{{offset.last_updated}}` by `1000`.

## Data Types

The `data_type` field specifies the type of data the parameter holds. The valid data types and their expected values are:

- **"LONG"**:
    - Represents a long integer (64-bit).
    - Acceptable values: `Integer`, `Long`, or numeric `String` that can be parsed to a `Long`.
- **"STRING"**:
    - Represents a sequence of characters.
    - Acceptable values: `String`.
- **"BOOLEAN"**:
    - Represents a boolean value.
    - Acceptable values: `Boolean` (`true` or `false`).
- **"RECORD"**:
    - Represents a map or object with key-value pairs.
    - Acceptable values: `Map<String, Object>`.
- **"LIST"**:
    - Represents an ordered collection of items.
    - Acceptable values: `List<Object>`.

**Note:** The value provided must match the specified `data_type`, or it will result in an error.

## Variables

The `variables` field is an array of key-value pairs that can be used within the API configuration.

**Variable Object Structure:**

- **param_name** (required): The name of the variable.
- **param_value** (required): The value of the variable.

**Example:**

```json
{
  "variables": [
    {
      "param_name": "base_url",
      "param_value": "https://api.example.com"
    },
    {
      "param_name": "version",
      "param_value": "v1"
    }
  ]
}
```

These variables can be referenced in parameters or URLs using the placeholder format.

## Full Example of API Configuration

Below is a complete example of an API configuration, including the use of different parameter types.

### JSON Example

```json
{
  "apis": [
    {
      "tag": "conversations",
      "url": "{{source_config.base_url}}/conversation_export",
      "method": "GET",
      "query_params": [
        {
          "id": 0,
          "name": "updated_before",
          "inputs": [
            {
              "id": 0,
              "value": "{{offset.updated_before}}",
              "data_type": "LONG",
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
        {
          "id": 1,
          "name": "updated_after",
          "inputs": [
            {
              "id": 0,
              "value": "{{offset.updated_after}}",
              "data_type": "LONG",
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
        }
      ],
      "variables": [
        {
          "param_name": "base_url",
          "param_value": "https://exports.dixa.io/v1"
        }
      ]
    }
  ]
}
```

**Explanation:**

- **API Definition:**
    - **tag**: `"conversations"`
    - **url**: Uses a variable placeholder `"{{source_config.base_url}}/conversation_export"`.
    - **method**: `"GET"`
- **Query Parameters:**
    - **"updated_before"** and **"updated_after"** parameters are computed using the `"multiply"` function.
    - Each function parameter takes two inputs:
        - A placeholder parameter retrieving a value from the `offset` configuration.
        - A constant parameter with the value `1000`.

**Function Execution:**

- The `"multiply"` function multiplies the `offset` value by `1000`.
- The result is used as the value for the respective query parameter.

## Constructing Parameters Step-by-Step

When creating parameters for your API configuration, follow these steps:

### 1. Determine the Parameter Type

- Decide whether the parameter is a constant value (`CONST`), a value from the context (`PLACE_HOLDER`), or a computed value (`FUNCTION`).

### 2. Assign a Unique ID

- Each parameter must have a unique `id` within its context (e.g., within `query_params`).

### 3. Specify the Name

- Use the `name` field to define the parameter's name as it should appear in the API request.

### 4. Define the Data Type

- Set the `data_type` field to the appropriate type based on the expected value.

### 5. Set the Ignore If Null Flag (Optional)

- If the parameter should be omitted when its value is `null`, set `ignore_if_null` to `true`.

### 6. Add Type-Specific Fields

- For **CONST** parameters:
    - Include the `value` field with the fixed value.
- For **PLACE_HOLDER** parameters:
    - Include the `value` field with the placeholder string.
- For **FUNCTION** parameters:
    - Include the `function` field with the function name.
    - Include the `inputs` field with an array of input parameters.
        - Each input parameter must be defined following these same steps.

### 7. Validate Data Types

- Ensure the `value` (for `CONST` and `PLACE_HOLDER`) or the function result matches the specified `data_type`.

### 8. Check Function Definitions

- Verify that any function used in a `FUNCTION` parameter is defined in the execution context and supports the provided inputs.

## Function Definitions

When using function parameters, the functions must be defined and available in the execution context.

**Example Function: `"multiply"`**

- **Purpose**: Multiplies two numbers.
- **Expected Inputs**:
    - Two parameters with `data_type` `"LONG"`.
- **Usage**:
    - Define a `FUNCTION` parameter with `function` set to `"multiply"`.
    - Provide two input parameters in the `inputs` array.

**Example Usage:**

```json
{
  "id": 6,
  "name": "total",
  "inputs": [
    {
      "id": 7,
      "value": 50,
      "data_type": "LONG",
      "parameter_type": "CONST"
    },
    {
      "id": 8,
      "value": 2,
      "data_type": "LONG",
      "parameter_type": "CONST"
    }
  ],
  "function": "multiply",
  "data_type": "LONG",
  "parameter_type": "FUNCTION"
}
```

**Note:** Ensure that the function and data types of inputs match the function's expected signature.

## Validation Rules and Error Handling

- **Unique IDs**: Each parameter must have a unique `id` within its parameter array.
- **Mandatory Fields**: Required fields (`id`, `data_type`, `parameter_type`, and type-specific fields) must be provided.
- **Placeholder Format**: Placeholders must follow the `"{{source_config.param}}"` format.
- **Data Type Matching**: The value's actual data type must match the specified `data_type`.
- **Function Input Validation**: Functions must receive the correct number and types of inputs.

**Common Errors:**

- **Invalid Data Type**: Occurs when the value doesn't match the specified `data_type`.
- **Missing Required Field**: Missing any required field will cause a validation error.
- **Incorrect Placeholder Format**: Placeholders not properly formatted will not be resolved.
- **Function Mismatch**: Using a function that doesn't exist or providing incorrect inputs will result in an error.

**Error Example:**

If a `PLACE_HOLDER` parameter's `value` is `"{{offset.updated_before"}` (missing closing braces), this will cause a validation error due to incorrect placeholder format.

## Best Practices

- **Consistent ID Assignment**: Assign IDs sequentially to maintain order and readability.
- **Clear Naming**: Use descriptive names for parameters to make the configuration self-explanatory.
- **Input Ordering**: When defining `inputs` for functions, ensure they are ordered correctly based on their `id`.
- **Testing Functions**: Verify functions with sample inputs to ensure they perform as expected before using them in the configuration.
- **Documentation**: Keep documentation of any custom functions used in the configuration for reference.

## Summary

This guide provides detailed instructions on constructing the `apis` part of your JSON configuration. By understanding parameter types, data types, and how to define functions and inputs, you can create complex and dynamic API configurations suitable for a variety of use cases. Always ensure that you follow the validation rules and best practices to avoid errors and maintain a robust configuration.

---

**Note:** This document is intended to be self-contained and provides all necessary information to construct the API configuration without referring to the underlying code.