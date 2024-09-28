# Source Configuration JSON Construction Guide

## Introduction

This document provides a detailed guide on constructing the `source_config` JSON object, which is used to configure the source settings for an API or data integration. The guide covers the structure, required fields, parameter types, data types, and includes examples to help you understand how to define your source configuration without referring to any underlying code.

---


## Overview

The `source_config` JSON object is designed to define the configuration settings required for connecting to a data source. It includes definitions of configuration variables, parameters for display purposes, and a unique identifier to distinguish different source configurations.

---

## Top-Level Structure

The JSON object has the following top-level structure:

```json
{
  "source_config": { ... }
}
```

### source_config

- **Type**: Object
- **Required**: Yes
- **Description**: Contains all the configuration settings for the source.
- **Properties**:
    - **config_definitions** (array, required)
    - **display_config** (array, optional)
    - **unique_source_identifier** (object, required)

---

## Fields in source_config

### config_definitions

- **Type**: Array of objects
- **Required**: Yes
- **Description**: Defines the configuration variables required by the source.

#### ConfigDefinition Object

Each object in the `config_definitions` array must include:

- **config_name** (string, required)
    - The name of the configuration variable.
- **type** (string, required)
    - The data type of the configuration variable.
    - Allowed values: `"BOOLEAN"`, `"STRING"`, `"INT"`, `"LONG"`, `"DOUBLE"`, `"PASSWORD"`
- **importance** (string, required)
    - The importance level of the configuration variable.
    - Allowed values: `"HIGH"`, `"MEDIUM"`, `"LOW"`
- **doc** (string, required)
    - Documentation or description of the configuration variable.
- **default_value** (varies, optional)
    - The default value of the configuration variable if not provided.

**Example**:

```json
{
  "config_name": "api_token",
  "type": "PASSWORD",
  "importance": "HIGH",
  "doc": "Enter the API Token that is linked to your account"
}
```

---

### display_config

- **Type**: Array of parameters
- **Required**: No
- **Description**: Defines parameters for display purposes in the configuration UI.

#### AbstractParameter Object

Each object in the `display_config` array is an **AbstractParameter**, which can be one of several parameter types.

- **Common Fields**:
    - **id** (integer, required): Unique identifier for the parameter.
    - **name** (string, optional): Name of the parameter.
    - **data_type** (string, required): Data type of the parameter's value.
    - **ignore_if_null** (boolean, optional): Whether to ignore the parameter if its value is null. Defaults to `false`.
    - **parameter_type** (string, required): Type of the parameter.

#### Parameter Types

1. **CONST**: Constant value.
    - **value** (required): The constant value.
    - **Example**:

      ```json
      {
        "id": 1,
        "name": "max_retries",
        "value": 5,
        "data_type": "INT",
        "parameter_type": "CONST"
      }
      ```

2. **PLACE_HOLDER**: References a value from the configuration context.
    - **value** (required): A placeholder string in the format `"{{config_name}}"` or `"{{config_name.parameter}}"`.
    - **Example**:

      ```json
      {
        "id": 2,
        "name": "api_endpoint",
        "value": "{{source_config.api_endpoint}}",
        "data_type": "STRING",
        "parameter_type": "PLACE_HOLDER"
      }
      ```

3. **FUNCTION**: Computes a value based on one or more input parameters.
    - **function** (required): Name of the function to compute the value.
    - **inputs** (array of parameters, required): Input parameters for the function.
    - **Example**:

      ```json
      {
        "id": 3,
        "name": "timeout",
        "function": "multiply",
        "inputs": [
          {
            "id": 4,
            "value": 2,
            "data_type": "INT",
            "parameter_type": "CONST"
          },
          {
            "id": 5,
            "value": "{{source_config.retry_delay}}",
            "data_type": "INT",
            "parameter_type": "PLACE_HOLDER"
          }
        ],
        "data_type": "INT",
        "parameter_type": "FUNCTION"
      }
      ```

---

### unique_source_identifier

- **Type**: Object
- **Required**: Yes
- **Description**: Defines a unique identifier for the source configuration, often used to prevent duplicate configurations.

#### UniqueSourceIdentifier Object

- **Properties**:
    - **identifier** (string, required)
        - A string that represents the unique identifier. It can include placeholders referencing parameters.
    - **parameters** (array of parameters, optional)
        - Parameters used within the identifier string.

**Rules and Constraints**:

- The `identifier` string can contain placeholders in the format `"{{parameter_name}}"`.
- All placeholders used in the `identifier` must correspond to the `name` fields of the parameters in the `parameters` array.
- The set of unique placeholders in the `identifier` must be a subset of the parameters' `name` set.

**Example**:

```json
{
  "identifier": "api_token",
  "parameters": [
    {
      "id": 0,
      "name": "api_token",
      "value": "{{source_config.api_token}}",
      "data_type": "STRING",
      "parameter_type": "PLACE_HOLDER"
    }
  ]
}
```

---

## Data Types

The `data_type` field specifies the type of data a parameter holds. Valid data types and their expected values are:

- **BOOLEAN**:
    - Represents a boolean value (`true` or `false`).
- **STRING**:
    - Represents a sequence of characters.
- **INT**:
    - Represents a 32-bit integer.
- **LONG**:
    - Represents a 64-bit integer.
- **DOUBLE**:
    - Represents a double-precision floating-point number.
- **PASSWORD**:
    - Represents sensitive information that should be handled securely.

---

## Constructing the JSON Object Step-by-Step

1. **Start with the Top-Level Structure**:

   ```json
   {
     "source_config": { ... }
   }
   ```

2. **Define config_definitions**:

    - Create an array under `"config_definitions"` containing one or more configuration variable definitions.
    - Each definition must include:
        - **config_name**: The name of the variable.
        - **type**: The data type of the variable.
        - **importance**: The importance level (`"HIGH"`, `"MEDIUM"`, `"LOW"`).
        - **doc**: A description of the variable.

   **Example**:

   ```json
   "config_definitions": [
     {
       "config_name": "api_token",
       "type": "PASSWORD",
       "importance": "HIGH",
       "doc": "Enter the API Token that is linked to your account"
     }
   ]
   ```

3. **Define display_config (Optional)**:

    - If display parameters are needed, create an array under `"display_config"`.
    - Each parameter in the array must follow the **AbstractParameter** structure.
    - Include the necessary fields based on the parameter type.

   **Example**:

   ```json
   "display_config": [
     {
       "id": 1,
       "name": "display_name",
       "value": "My Data Source",
       "data_type": "STRING",
       "parameter_type": "CONST"
     }
   ]
   ```

4. **Define unique_source_identifier**:

    - Create an object under `"unique_source_identifier"`.
    - Include the **identifier** string, which may contain placeholders.
    - If the identifier uses placeholders, define the corresponding parameters in the `"parameters"` array.

   **Example**:

   ```json
   "unique_source_identifier": {
     "identifier": "{{api_token}}",
     "parameters": [
       {
         "id": 0,
         "name": "api_token",
         "value": "{{source_config.api_token}}",
         "data_type": "STRING",
         "parameter_type": "PLACE_HOLDER"
       }
     ]
   }
   ```

   **Important**:

    - Ensure that all placeholders in the `identifier` match the `name` fields in the `parameters` array.
    - The set of placeholders must be a subset of the parameter names.

5. **Combine All Components**:

    - Assemble all the components under the `"source_config"` key.

---

## Example JSON Object

```json
{
  "source_config": {
    "config_definitions": [
      {
        "config_name": "api_token",
        "type": "PASSWORD",
        "importance": "HIGH",
        "doc": "Enter the API Token that is linked to your account"
      }
    ],
    "display_config": [],
    "unique_source_identifier": {
      "identifier": "api_token",
      "parameters": [
        {
          "id": 0,
          "name": "api_token",
          "value": "{{source_config.api_token}}",
          "data_type": "STRING",
          "parameter_type": "PLACE_HOLDER"
        }
      ]
    }
  }
}
```

**Explanation**:

- **config_definitions**:
    - Defines a single configuration variable `api_token` of type `PASSWORD`.
- **display_config**:
    - Empty array, indicating no display parameters are defined.
- **unique_source_identifier**:
    - The identifier is `"api_token"`, directly referencing the configuration variable.
    - The parameters array includes a single parameter that references `{{source_config.api_token}}`.

---

## Validation Rules and Best Practices

- **Required Fields**:
    - Ensure all required fields are included in each object.
- **Unique IDs**:
    - Assign unique `id` values to each parameter within their respective arrays.
- **Parameter Names**:
    - Use clear and descriptive names for parameters.
- **Placeholder Matching**:
    - All placeholders in the `identifier` must match the `name` fields in the `parameters` array.
    - The set of unique placeholders in the `identifier` must be a subset of the parameters' names.
- **Data Type Consistency**:
    - Ensure that the `data_type` of each parameter matches the expected type of the value provided.
- **Parameter Types**:
    - Use the correct `parameter_type` for each parameter (`CONST`, `PLACE_HOLDER`, or `FUNCTION`).
- **Documentation**:
    - Provide clear and helpful documentation in the `doc` field for each configuration variable.
- **Secure Handling of Sensitive Data**:
    - For sensitive data like passwords or API tokens, use the `PASSWORD` data type and handle securely.

---

## Conclusion

This guide provides detailed instructions on constructing the `source_config` JSON object for configuring source settings in your application or integration. By carefully defining configuration variables, display parameters, and unique identifiers, you can ensure that your source configurations are valid, functional, and secure.

Remember to:

- Include all required fields and follow the specified structure.
- Validate your configuration against the provided rules and best practices.
- Use clear and descriptive names and documentation to enhance readability and maintainability.

---

**Note**: This document is exhaustive and includes all necessary details to understand and construct the `source_config` JSON object without any reference to external code or omitted information.