/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * OpenTSDB 2.0 jersey based REST client
 *
 * @author Sean Scanlon <sean.scanlon@gmail.com>
 */
public class OpenTsdb {

    public static final int DEFAULT_BATCH_SIZE_LIMIT = 0;
    public static final int CONN_TIMEOUT_DEFAULT_MS = 300_000;
    public static final int READ_TIMEOUT_DEFAULT_MS = 300_000;
    private static final Logger logger = LoggerFactory.getLogger(OpenTsdb.class);
    private Client client;
    private final WebTarget apiResource;
    private int batchSizeLimit = DEFAULT_BATCH_SIZE_LIMIT;


    private OpenTsdb(WebTarget apiResource) {
        this.apiResource = apiResource;
    }

    private OpenTsdb(String baseURL, Integer connectionTimeout, Integer readTimeout) {
        this.client = ClientBuilder.newBuilder()
                .register(JacksonFeature.class)
                .build();


        client.property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout);
        client.property(ClientProperties.READ_TIMEOUT, readTimeout);

        this.apiResource = client.target(baseURL);
    }

    public static class Builder {

        private Integer connectionTimeout = CONN_TIMEOUT_DEFAULT_MS;
        private Integer readTimeout = READ_TIMEOUT_DEFAULT_MS;
        private final String baseUrl;

        public Builder(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Builder withConnectTimeout(Integer connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder withReadTimeout(Integer readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public OpenTsdb create() {
            return new OpenTsdb(baseUrl, connectionTimeout, readTimeout);
        }
    }

    /**
     * Initiate a client Builder with the provided base opentsdb server url.
     *
     * @param baseUrl
     * @return
     */
    public static Builder forService(String baseUrl) {
        return new Builder(baseUrl);
    }

    /**
     * create a client by providing the underlying WebResource
     *
     * @param apiResource
     * @return
     */
    public static OpenTsdb create(WebTarget apiResource) {
        return new OpenTsdb(apiResource);
    }



    public boolean isResponding(){
        try {
            Response response = apiResource.path("/").request().get();
            int status = response.getStatus();
            if (status == 200) {
                return true;
            }
        } catch (Exception e){
            //
        }
        return false;
    }

    public void close(){
        try {
            client.close();
        } catch (Exception e){
            // don't care if null pointer or anything else.
        }

    }

    public String preAssignMetric(String metric) {
        try {
            return apiResource.path("/api/uid/assign")
                    .queryParam("metric", metric)
                    .request(MediaType.APPLICATION_JSON)
                    .get(String.class);
        } catch (Exception e) {
            logger.info("Exception occurred while pre-assign metric");
        }

        return "";

    }

    public String preAssignDimensions(Set<String> metaData) {

        String tagks = metaData.stream().reduce((s, s2) -> s + "," + s2).get();
        try {
            return apiResource.path("/api/uid/assign")
                    .queryParam("tagk", tagks)
                    .request(MediaType.APPLICATION_JSON)
                    .get(String.class);
        } catch (Exception e) {
            logger.info("Exception occurred while pre-assign dimensions", e);
        }
        return "";

    }

    public String preAssignValues(Set<String> metaData) {

        String tagkv = metaData.stream().reduce((s, s2) -> s + "," + s2).get();
        try {
            return apiResource.path("/api/uid/assign")
                    .queryParam("tagv", tagkv)
                    .request(MediaType.APPLICATION_JSON)
                    .get(String.class);
        } catch (Exception e) {
            logger.info("Exception occurred while pre-assign dim values", e);
        }
        return "";
    }

    public void setBatchSizeLimit(int batchSizeLimit) {
        this.batchSizeLimit = batchSizeLimit;
    }



    public String query(String start, String end, String metric, String tags) {
        return queryHelper(metric, start, end, tags);
    }

    public String query2(String start, String end, String[] metrics, String[] tags) {
        return queryHelper2(metrics, start, end, tags);
    }

    public String query4(String startDate, String endDate, String[] queriesEx, String[] tagsEx) {
        return queryHelper4(queriesEx, startDate, endDate, tagsEx);
    }

    public String query6(String start, String end, String[] metrics, String[] tags) {
        return queryHelper6(metrics, start, end, tags);
    }

    public String query12(String startDate, String endDate, String[] queries, String[] tags) {
        return queryHelper12(queries, startDate, endDate, tags);
    }


    private String queryHelper(String metric, String start, String end, String tags) {
        try {

            return apiResource.path("/api/query")
                    .queryParam("m", metric)
                    .resolveTemplate("tags", tags)
                    .queryParam("start", start)
                    .queryParam("end", end).
                            request(MediaType.TEXT_PLAIN)
                    .get(String.class);

        } catch (Exception ex) {
            logger.error("query to opentsdb endpoint failed for metric: {} with tags {} and start: {} and end {}", metric, start, end, tags, ex);
        }

        return null;
    }

    private String queryHelper2(String[] metrics, String start, String end, String[] tags) {
        try {
            WebTarget req = apiResource.path("/api/query")
                    .queryParam("m", metrics[0])
                    .resolveTemplate("tags0", tags[0])
                    .queryParam("m", metrics[1])
                    .resolveTemplate("tags1", tags[1]);
            return req.queryParam("start", start)
                    .queryParam("end", end).
                            request(MediaType.TEXT_PLAIN)
                    .get(String.class);

        } catch (Exception ex) {
            logger.error("query to opentsdb endpoint failed for metrics : {} with tags {} and start: {} and end {}", metrics, start, end, tags, ex);
        }

        return null;
    }

    private String queryHelper4(String[] metrics, String start, String end, String[] tags) {
        try {
            WebTarget req = apiResource.path("/api/query")
                    .queryParam("m", metrics[0])
                    .resolveTemplate("tags0", tags[0])
                    .queryParam("m", metrics[1])
                    .resolveTemplate("tags1", tags[1])
                    .queryParam("m", metrics[2])
                    .resolveTemplate("tags2", tags[2])
                    .queryParam("m", metrics[3])
                    .resolveTemplate("tags3", tags[3]);
            return req.queryParam("start", start)
                    .queryParam("end", end).
                            request(MediaType.TEXT_PLAIN)
                    .get(String.class);

        } catch (Exception ex) {
            logger.error("query to opentsdb endpoint failed for metrics : {} with tags {} and start: {} and end {}", metrics, start, end, tags, ex);
        }

        return null;
    }

    private String queryHelper6(String[] metrics, String start, String end, String[] tags) {
        try {
            WebTarget req = apiResource.path("/api/query")
                    .queryParam("m", metrics[0])
                    .resolveTemplate("tags0", tags[0])
                    .queryParam("m", metrics[1])
                    .resolveTemplate("tags1", tags[1])
                    .queryParam("m", metrics[2])
                    .resolveTemplate("tags2", tags[2])
                    .queryParam("m", metrics[3])
                    .resolveTemplate("tags3", tags[3])
                    .queryParam("m", metrics[4])
                    .resolveTemplate("tags4", tags[4])
                    .queryParam("m", metrics[5])
                    .resolveTemplate("tags5", tags[5]);


            return req.queryParam("start", start)
                    .queryParam("end", end).
                            request(MediaType.TEXT_PLAIN)
                    .get(String.class);

        } catch (Exception ex) {
            logger.error("query to opentsdb endpoint failed for metrics : {} with tags {} and start: {} and end {}", metrics, start, end, tags, ex);
        }

        return null;
    }

    private String queryHelper12(String[] metrics, String start, String end, String[] tags) {
        try {
            WebTarget req = apiResource.path("/api/query")
                    .queryParam("m", metrics[0])
                    .resolveTemplate("tags0", tags[0])
                    .queryParam("m", metrics[1])
                    .resolveTemplate("tags1", tags[1])
                    .queryParam("m", metrics[2])
                    .resolveTemplate("tags2", tags[2])
                    .queryParam("m", metrics[3])
                    .resolveTemplate("tags3", tags[3])
                    .queryParam("m", metrics[4])
                    .resolveTemplate("tags4", tags[4])
                    .queryParam("m", metrics[5])
                    .resolveTemplate("tags5", tags[5])
                    .queryParam("m", metrics[6])
                    .resolveTemplate("tags6", tags[6])
                    .queryParam("m", metrics[7])
                    .resolveTemplate("tags7", tags[7])
                    .queryParam("m", metrics[8])
                    .resolveTemplate("tags8", tags[8])
                    .queryParam("m", metrics[9])
                    .resolveTemplate("tags9", tags[9])
                    .queryParam("m", metrics[10])
                    .resolveTemplate("tags10", tags[10])
                    .queryParam("m", metrics[11])
                    .resolveTemplate("tags11", tags[11]);


            return req.queryParam("start", start)
                    .queryParam("end", end).
                            request(MediaType.TEXT_PLAIN)
                    .get(String.class);

        } catch (Exception ex) {
            logger.error("query to opentsdb endpoint failed for metrics : {} with tags {} and start: {} and end {}", metrics, start, end, tags, ex);
        }

        return null;
    }


    /**
     * Send a metric to opentsdb
     *
     * @param metric
     */
    public boolean send(OpenTsdbMetric metric) {
        return send(Collections.singleton(metric));
    }

    /**
     * send a set of metrics to opentsdb
     *
     * @param metrics
     */
    public boolean send(Set<OpenTsdbMetric> metrics) {
        // we set the patch size because of existing issue in opentsdb where large batch of metrics failed
        // see at https://groups.google.com/forum/#!topic/opentsdb/U-0ak_v8qu0
        // we recommend batch size of 5 - 10 will be safer
        // alternatively you can enable chunked request
        if (batchSizeLimit > 0 && metrics.size() > batchSizeLimit) {
            final Set<OpenTsdbMetric> smallMetrics = new HashSet<>();
            for (final OpenTsdbMetric metric : metrics) {
                smallMetrics.add(metric);
                if (smallMetrics.size() >= batchSizeLimit) {
                    sendHelper(smallMetrics);
                    smallMetrics.clear();
                }
            }
            return sendHelper(smallMetrics);
        } else {
            return sendHelper(metrics);
        }
    }

    private boolean sendHelper(Set<OpenTsdbMetric> metrics) {
        /*
         * might want to bind to a specific version of the API.
         * according to: http://opentsdb.net/docs/build/html/api_http/index.html#api-versioning
         * "if you do not supply an explicit version, ... the latest version will be used."
         * circle back on this if it's a problem.
         */
        if (!metrics.isEmpty()) {
            try {
                final Entity<?> entity = Entity.entity(metrics, MediaType.APPLICATION_JSON);
                apiResource.path("/api/put").request().post(entity);
                return true;
            } catch (Exception ex) {
                logger.debug("send to opentsdb endpoint failed", ex);
                return false;
            }
        }
        return true;
    }

}