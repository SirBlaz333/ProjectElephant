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

    @Test
    void testReadStatementWithDollarQuotedString() throws IOException {
        String script = "$$Dollar quoted string$$; SELECT * FROM table;";
        Reader reader = new StringReader(script);
        ScriptReader scriptReader = new ScriptReader(reader);

        assertEquals("$$Dollar quoted string$$", scriptReader.readStatement().trim());
        assertEquals("SELECT * FROM table", scriptReader.readStatement().trim());
        assertNull(scriptReader.readStatement());
    }

    @Test
    void testReadStatementWithNestedBlockComments() throws IOException {
        String script = "/* Outer comment /* Nested comment */ Outer continuation */ SELECT * FROM table;";
        Reader reader = new StringReader(script);
        ScriptReader scriptReader = new ScriptReader(reader);

        assertEquals("/* Outer comment /* Nested comment */ Outer continuation */ SELECT * FROM table", scriptReader.readStatement().trim());
        assertNull(scriptReader.readStatement()); // EOF
    }
    @Test
    void testReadStatementWithEOFInStringLiteral() throws IOException {
        String script = "SELECT * FROM table WHERE name = 'incomplete";
        Reader reader = new StringReader(script);
        ScriptReader scriptReader = new ScriptReader(reader);

        assertNotNull(scriptReader.readStatement());
    }

    @Test
    void testReadStatementWithQuotes() throws IOException {
        String script = "SELECT * FROM table WHERE name = \"incomplete\"";
        Reader reader = new StringReader(script);
        ScriptReader scriptReader = new ScriptReader(reader);

        assertNotNull(scriptReader.readStatement());
    }

    @Test
    void testReadStatementWithEOFInBlockComment() throws IOException {
        String script = "/* Incomplete block comment";
        Reader reader = new StringReader(script);
        ScriptReader scriptReader = new ScriptReader(reader);

        assertNotNull(scriptReader.readStatement());
    }

    @Test
    void testIOExceptionOnRead() throws IOException {
        Reader reader = new StringReader("SELECT * FROM DUAL;") {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Read error");
            }
        };
        ScriptReader scriptReader = new ScriptReader(reader);

        assertThrows(RuntimeException.class, scriptReader::readStatement);
    }

    @Test
    void testLargeSQLStatement() throws IOException {
        String largeSQL = "SELECT '" + new String(new char[1000]).replace("\0", "a") + "';";
        ScriptReader scriptReader = new ScriptReader(new StringReader(largeSQL));

        assertNotNull(scriptReader.readStatement());
        assertNull(scriptReader.readStatement());
    }

    @Test
    void testEndOfFileWithoutSemicolon() throws IOException {
        String script = "SELECT * FROM table WHERE id = 1";
        ScriptReader scriptReader = new ScriptReader(new StringReader(script));

        assertNotNull(scriptReader.readStatement());
        assertNull(scriptReader.readStatement());
    }
}