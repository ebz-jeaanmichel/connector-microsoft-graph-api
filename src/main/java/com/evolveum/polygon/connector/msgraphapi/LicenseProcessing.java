package com.evolveum.polygon.connector.msgraphapi;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.json.JSONArray;
import org.json.JSONObject;


public class LicenseProcessing extends ObjectProcessing {
    public static final String OBJECT_CLASS_NAME = "license";
    public static final ObjectClass OBJECT_CLASS = new ObjectClass(OBJECT_CLASS_NAME);

    private static String GRAPH_SUBSCRIBEDSKUS = "/subscribedSkus";

    public static final String ATTR_ID = "id";
    public static final String ATTR_APPLIESTO = "appliesTo";
    public static final String ATTR_SKUID = "skuId";
    public static final String ATTR_SKUPAATNUMBER = "skuPartNumber";
    public static final String ATTR_CAPABILITYSTATUS = "capabilityStatus";
    public static final String ATTR_CONSUMEDUNITS = "consumedUnits";
    // prepaid units
    public static final String ATTR_PREPAIDUNITS = "prepaidUnits";
    public static final String ATTR_ENABLED = "enabled";
    public static final String ATTR_SUSPENDED = "suspended";
    public static final String ATTR_WARNING = "warning";
    public static final String ATTR_PREPAIDUNITS__ENABLED = ATTR_PREPAIDUNITS + "." + ATTR_ENABLED;
    // service plans
    public static final String ATTR_SERVICEPLANS = "servicePlans";
    public static final String ATTR_SERVICEPLANID = "servicePlanId";
    public static final String ATTR_SERVICEPLANS__SERVICEPLANID = ATTR_SERVICEPLANS + "." + ATTR_SERVICEPLANID;

    private static final String SELECTOR_FULL = selector(
            ATTR_ID,
            ATTR_APPLIESTO,
            ATTR_SKUID,
            ATTR_SKUPAATNUMBER,
            ATTR_CAPABILITYSTATUS,
            ATTR_CONSUMEDUNITS,
            ATTR_PREPAIDUNITS,
            ATTR_SERVICEPLANS
    );
    private static final String SELECTOR_PARTIAL = selector(
            ATTR_ID,
            ATTR_APPLIESTO,
            ATTR_SKUID,
            ATTR_SKUPAATNUMBER,
            ATTR_CAPABILITYSTATUS,
            ATTR_CONSUMEDUNITS,
            ATTR_PREPAIDUNITS
    );

    public LicenseProcessing(GraphEndpoint graphEndpoint, SchemaTranslator schemaTranslator) {
        super(graphEndpoint, ICFPostMapper.builder().build());
    }

    public void buildLicenseObjectClass(SchemaBuilder schemaBuilder) {
        schemaBuilder.defineObjectClass(objectClassInfo());
    }

    @Override
    protected String type() {
        return OBJECT_CLASS_NAME;
    }

    @Override
    protected ObjectClassInfo objectClassInfo() {
        Set<AttributeInfo> attributes = new HashSet<>();

        attributes.add(new AttributeInfoBuilder(ATTR_ID)
                .setCreateable(false)
                .setUpdateable(false)
                .build());
        attributes.add(new AttributeInfoBuilder(ATTR_APPLIESTO)
                .setCreateable(false)
                .setUpdateable(false)
                .build());
        attributes.add(new AttributeInfoBuilder(ATTR_CAPABILITYSTATUS)
                .setCreateable(false)
                .setUpdateable(false)
                .build());
        attributes.add(new AttributeInfoBuilder(ATTR_CONSUMEDUNITS)
                .setCreateable(false)
                .setUpdateable(false)
                .setType(Integer.class)
                .build());
        attributes.add(new AttributeInfoBuilder(ATTR_SKUID)
                .setCreateable(false)
                .setUpdateable(false)
                .build());
        attributes.add(new AttributeInfoBuilder(ATTR_SKUPAATNUMBER)
                .setCreateable(false)
                .setUpdateable(false)
                .build());
        attributes.add(new AttributeInfoBuilder(ATTR_PREPAIDUNITS__ENABLED)
                .setCreateable(false)
                .setUpdateable(false)
                .setType(Integer.class)
                .setMultiValued(true)
                .build());
        attributes.add(new AttributeInfoBuilder(ATTR_SERVICEPLANS__SERVICEPLANID)
                .setCreateable(false)
                .setUpdateable(false)
                .setMultiValued(true)
                .build());
        return new ObjectClassInfoBuilder()
                .setType(type())
                .addAllAttributeInfo(attributes)
                .build();
    }

    private void get(ResultsHandler handler, String skuId, OperationOptions options) {
        final GraphEndpoint endpoint = getGraphEndpoint();
        JSONObject json = endpoint.executeGetRequest(GRAPH_SUBSCRIBEDSKUS + "/" + skuId, SELECTOR_FULL, options);
        LOG.info("JSONObject license {0}", json);
        handleJSONObject(options, json, handler);
    }

    private void list(ResultsHandler handler, OperationOptions options) {
        final GraphEndpoint endpoint = getGraphEndpoint();
        String selector = SELECTOR_FULL;
        if (options != null && options.getAllowPartialAttributeValues() != null && options.getAllowPartialAttributeValues())
            selector = SELECTOR_PARTIAL;
        // Paging is not supported
        endpoint.executeListRequest(GRAPH_SUBSCRIBEDSKUS, selector, options, false, createJSONObjectHandler(handler));
    }

    public List<JSONObject> list() {
        final GraphEndpoint endpoint = getGraphEndpoint();
        // Paging is not supported
        JSONArray json = endpoint.executeListRequest(GRAPH_SUBSCRIBEDSKUS, SELECTOR_FULL, null, false);
        LOG.info("JSONObject license {0}", json);
        return handleJSONArray(json);
    }

    public void executeQueryForLicense(Filter query, ResultsHandler handler, OperationOptions options) {
        LOG.info("executeQueryForLicense()");

        if (query instanceof EqualsFilter) {
            final EqualsFilter equalsFilter = (EqualsFilter) query;
            final Attribute attr = equalsFilter.getAttribute();
            final String attrName = attr.getName();
            LOG.info("query instanceof EqualsFilter");

            if (attrName.equals(ATTR_ID) || attrName.equals(Uid.NAME)) {
                String value = AttributeUtil.getAsStringValue(attr);
                if (value == null)
                    invalidAttributeValue("Uid", query);
                get(handler, value, options);
                return;
            }
        }

        list(handler, options);
    }

    @Override
    protected boolean handleJSONObject(OperationOptions options, JSONObject json, ResultsHandler handler) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setObjectClass(OBJECT_CLASS);

        getUIDIfExists(json, ATTR_ID, builder);
        getNAMEIfExists(json, ATTR_SKUPAATNUMBER, builder);

        getIfExists(json, ATTR_ID, String.class, builder);
        getIfExists(json, ATTR_APPLIESTO, String.class, builder);
        getIfExists(json, ATTR_CAPABILITYSTATUS, String.class, builder);
        getIfExists(json, ATTR_CONSUMEDUNITS, Integer.class, builder);
        getIfExists(json, ATTR_SKUID, String.class, builder);
        getIfExists(json, ATTR_SKUPAATNUMBER, String.class, builder);
        getFromItemIfExists(json, ATTR_PREPAIDUNITS, ATTR_ENABLED, Integer.class, builder);
        getFromArrayIfExists(json, ATTR_SERVICEPLANS, ATTR_SERVICEPLANID, String.class, builder);

        return handler.handle(builder.build());
    }

    public String getNameAttribute(){

        return ATTR_SKUPAATNUMBER;
    }

    public String getUIDAttribute(){

        return ATTR_ID;
    }

}
