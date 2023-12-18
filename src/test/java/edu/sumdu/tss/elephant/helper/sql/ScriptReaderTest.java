package edu.sumdu.tss.elephant.helper.sql;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class ScriptReaderTest {

    @Test
    void close() throws IOException {
        Reader mockReader = Mockito.mock(Reader.class);
        ScriptReader scriptReader = new ScriptReader(mockReader);

        assertDoesNotThrow(() -> scriptReader.close());

        verify(mockReader).close();
    }

    @Test
    void readStatement() throws IOException {
        String script = "SELECT * FROM table1; SELECT * FROM table2;";
        Reader reader = new StringReader(script);
        ScriptReader scriptReader = new ScriptReader(reader);

        String statement1 = scriptReader.readStatement();
        String statement2 = scriptReader.readStatement();
        String statement3 = scriptReader.readStatement();

        assertEquals("SELECT * FROM table1", statement1);
        assertEquals(" SELECT * FROM table2", statement2);
        assertNull(statement3);
    }

    @Test
    void isInsideRemark() throws IOException {
        String script = "/* This is a block comment */ SELECT * FROM table;";
        Reader reader = new StringReader(script);
        ScriptReader scriptReader = new ScriptReader(reader);

        boolean insideRemark1 = scriptReader.isInsideRemark();
        scriptReader.readStatement();
        boolean insideRemark2 = scriptReader.isInsideRemark();

        assertFalse(insideRemark1);
        assertFalse(insideRemark2);
    }

    @Test
    void isBlockRemark() throws IOException {
        String script = "/* This is a block comment */ SELECT * FROM table;";
        Reader reader = new StringReader(script);
        ScriptReader scriptReader = new ScriptReader(reader);

        scriptReader.readStatement();
        boolean isBlockRemark = scriptReader.isBlockRemark();

        assertTrue(isBlockRemark);
    }

    @Test
    void setSkipRemarks() {
        String script = "-- This is a single line comment\n SELECT * FROM table;";
        Reader reader = new StringReader(script);
        ScriptReader scriptReader = new ScriptReader(reader);

        scriptReader.setSkipRemarks(true);
        scriptReader.readStatement();
        boolean insideRemark = scriptReader.isInsideRemark();

        assertFalse(insideRemark);
    }
}