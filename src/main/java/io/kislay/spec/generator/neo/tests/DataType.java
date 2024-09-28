package io.kislay.spec.generator.neo.tests;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public enum DataType {
    LONG {
        @Override
        public Long value(Object value) {
            try {
                if (Objects.isNull(value)) {
                    return null;
                }
                if (value instanceof Integer) {
                    return ((Integer) value).longValue();
                } else if (value instanceof Long) {
                    return (Long) value;
                } else if (value instanceof String) {
                    return Long.valueOf((String) value);
                }
                throw new ClassCastException();
            } catch (ClassCastException | NumberFormatException e) {
                throw new RuntimeException("Invalid Data type.", e);
            }
        }
    },
    STRING {
        @Override
        public String value(Object value) {
            try {
                return (String) value;
            } catch (ClassCastException e) {
                throw new RuntimeException("Invalid Data type.", e);
            }
        }
    },
    BOOLEAN {
        @Override
        public Boolean value(Object value) {
            try {
                return (Boolean) value;
            } catch (ClassCastException e) {
                throw new RuntimeException("Invalid Data type.", e);
            }
        }
    },
    RECORD {
        @Override
        public Map<String, Object> value(Object value) {
            try {
                if (value instanceof Map) {
                    return (Map<String, Object>) value;
                } else {
                    throw new RuntimeException("Value not an instance of Map.");
                }
            } catch (ClassCastException e) {
                throw new RuntimeException("Invalid Data type.", e);
            }
        }
    },
    LIST {
        @Override
        public List<Object> value(Object value) {
            try {
                if (value instanceof List) {
                    return (List<Object>) value;
                } else {
                    throw new RuntimeException("Value not an instance of List.");
                }
            } catch (ClassCastException e) {
                throw new RuntimeException("Invalid Data type.", e);
            }
        }
    };

    public abstract Object value(Object value);
}