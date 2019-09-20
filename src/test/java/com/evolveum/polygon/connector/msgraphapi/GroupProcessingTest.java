package com.evolveum.polygon.connector.msgraphapi;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.testng.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Test case for {@link GroupProcessing}
 */
public class GroupProcessingTest {

    private JSONObject parseResource(String fileName) throws IOException  {
        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            return new JSONObject(IOUtils.toString(is));
        }
    }

    final GroupProcessing groupProcessing = new GroupProcessing(new MSGraphConfiguration());

    @Test
    public void testParseGroupMembers() throws Exception {
        final JSONObject membersJson = parseResource("groupMembers.json");
        final JSONArray jarr = groupProcessing.getJSONArray(membersJson, "id");
        final List<Object> ids = jarr.toList();
        Assert.assertEquals(2, ids.size());
        Assert.assertEquals("9639bcbc-0089-4855-a793-44b940e52286", ids.get(0));
        Assert.assertEquals("f034f71e-22a8-489b-8492-f5f7133559c1", ids.get(1));
    }
}
