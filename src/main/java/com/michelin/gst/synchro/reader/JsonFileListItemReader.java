package com.michelin.gst.synchro.reader;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.file.separator.RecordSeparatorPolicy;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.List;

@SuppressWarnings("rawtypes")
public class JsonFileListItemReader extends AbstractItemCountingItemStreamItemReader implements ResourceAwareItemReaderItemStream, InitializingBean {

    private static final Log logger = LogFactory.getLog(JsonFileListItemReader.class);

    private Resource resource;

    private List items;
    private String classToBound;
    private int index = 0;

    public void setClassToBound(String classToBound) {
        this.classToBound = classToBound;
    }

    ObjectMapper mapper = new ObjectMapper();

    public JsonFileListItemReader() {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        setName(ClassUtils.getShortName(JsonFileListItemReader.class));
    }

    /**
     * Public setter for the input resource.
     * @param resource
     */
    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * @return string corresponding to logical record according to
     * {@link #setRecordSeparatorPolicy(RecordSeparatorPolicy)} (might span
     * multiple lines in file).
     * @throws java.lang.Exception
     */
    @Override
    protected Object doRead() throws Exception {
        return (this.items == null || index >= this.items.size()) ? null : this.items.get(index++);
    }

    @Override
    protected void doClose() throws Exception {}

    @Override
    protected void doOpen() throws Exception {
        Assert.notNull(resource, "Input resource must be set");

        if (!resource.exists()) {
            logger.warn("Input resource does not exist " + resource.getDescription());
            return;
        }

        if (!resource.isReadable()) {
            logger.warn("Input resource is not readable " + resource.getDescription());
            return;
        }

        try {
            this.items = this.mapper.readValue(resource.getInputStream(),
                    TypeFactory.defaultInstance().constructCollectionType(List.class, Class.forName(this.classToBound)));
            logger.info("########### mapping successfuly,  filename: " + resource.getFilename());
        } catch (Exception ex) {
            throw new ParseException("Parsing error", ex);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {}
}

