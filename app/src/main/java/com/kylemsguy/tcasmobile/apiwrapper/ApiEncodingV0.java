package com.kylemsguy.tcasmobile.apiwrapper;

import java.io.UnsupportedEncodingException;

/**
 * TwoCans structured request encoding version 0. 
 * This encoding was designed on a napkin. It is simple, compact-ish and quick.
 * Because I fully intend to get fancier later in TwoCans3 which is coming soon, 
 * I have given it the name v0 instead of v1.
 *
 * <p>Essentially, this is just a encoding for a 2D uneven array of objects.
 * Rows are separated by commas.
 * Items in each row are separated by vertical pipes '|'.
 * Strings, integers, floats, booleans, and nulls are supported as items in each
 * row. They have the following encodings:
 * <ul>
 * <li>Null: 'n'</li>
 * <li>Boolean: 'B' for true, 'b' for false</li>
 * <li>Integer: 'i' followed by a decimal representation</li>
 * <li>Float: 'f' followed by a reasonable toString output of the value</li>
 * <li>String: '$' followed by a hex encoded representation of the raw binary value</li>
 * </ul>
 *
 * <p>TODO: fix unicode support for strings. Currently this is a raw repeater of 
 * the ASCII values returned. Need to pass the decoded bytes through a decoding method.
 */
final class ApiEncodingV0 extends ApiEncoding {
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    
    public ApiEncodingV0() {
        super("v0|");
    }
    
    public Object decode(String rawValue) {
        return decodeImpl(rawValue);
    }
    
    public Object[][] decodeImpl(String rawValue) {
        String prefix = getVersionPrefix();
        if (prefix.equals(rawValue.substring(0, prefix.length()))) {
            // trim encoding prefix
            rawValue = rawValue.substring(prefix.length());
        } else {
            // TODO: custom exception
            throw new IllegalStateException("Received an invalid encoding.");
        }
        
        String[] rows = rawValue.split(",");
        Object[][] output = new Object[rows.length][];
        for (int y = 0; y < rows.length; ++y) {
            String[] rawRow = rows[y].split("\\|");
            int width = rawRow.length;
            if (width == 1 && rawRow[0].length() == 0) width = 0;
            Object[] row = new Object[width];
            for (int x = 0; x < width; ++x) {
                row[x] = decodeItem(rawRow[x]);
            }
            output[y] = row;
        }
        return output;
    }
    
    private static Object decodeItem(String rawValue) {
        if (rawValue.length() > 0) {
            switch (rawValue.charAt(0)) {
                case 'n': return null;
                case 'B': return true;
                case 'b': return false;
                case 'i': return decodeInteger(rawValue.substring(1));
                case 'f': return decodeFloat(rawValue.substring(1));
                case '$': return decodeString(rawValue.substring(1));
                default: break;
            }
        }
        throw new IllegalStateException("Encountered unknown type.");
    }
    
    private static int decodeInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            throw new IllegalStateException("Encountered unrecognized value: " + value);
        }
    }
    
    private static double decodeFloat(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            throw new IllegalStateException("Encountered unrecognized value: " + value);
        }
    }
    
    private static String decodeString(String value) {
        int length = value.length() / 2;
        char[] output = new char[length];
        String hex;
        for (int i = 0; i < length; ++i) {
            hex = value.substring(i * 2, i * 2 + 2);
            try {
                output[i] = (char) (Integer.parseInt(hex, 16) & 255);
            } catch (NumberFormatException nfe) {
                throw new IllegalStateException("Encountered unrecognized value: " + value + " | " + hex);
            }
        }
        return new String(output);
    }
    
    /**
     * Table is an array of objects or an array of array of objects.
     */
    public String encode(Object value) {
        if (!(value instanceof Object[])) value = new Object[] { value };
        Object[] table = (Object[]) value;
        StringBuilder output = new StringBuilder();
        output.append("v0|");
        boolean isSingleDimensional = !(table[0] instanceof Object[]);
        if (isSingleDimensional) {
            table = new Object[] { table };
        }
        
        for (int i = 0; i < table.length; ++i) {
            if (i > 0) output.append(",");
            Object[] row = (Object[]) table[i];
            for (int j = 0; j < row.length; ++j) {
                Object item = row[j];
                if (j > 0) output.append("|");
                if (item instanceof String) encodeString((String) item, output);
                else if (item instanceof Integer) encodeInteger((Integer) item, output);
                else if (item instanceof Double) encodeFloat((Double) item, output);
                else if (item instanceof Float) encodeFloat((Float) item, output);
                else if (item instanceof Boolean) encodeBoolean((Boolean) item, output);
                else if (item == null) encodeNull(output);
                else throw new IllegalStateException("Invalid data type sent to encoder.");
            }
        }
        
        return output.toString();
    }
    
    private static void encodeNull(StringBuilder output) {
        output.append("n");
    }
    
    private static void encodeBoolean(boolean value, StringBuilder output) {
        output.append(value ? "B" : "b");
    }
    
    private static void encodeFloat(double value, StringBuilder output) {
        output.append("f" + value);
    }
    
    private static void encodeInteger(int value, StringBuilder output) {
        output.append("i" + value);
    }
    
    private static void encodeString(String value, StringBuilder output) {
        output.append("$");
        byte[] bytes = null;
        try {
            bytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException("Universe is broken.");
        }
        int length = bytes.length;
        for (int i = 0; i < length; ++i) {
            output.append(HEX[(bytes[i] >> 4) & 15]);
            output.append(HEX[bytes[i] & 15]);
        }
    }
}
