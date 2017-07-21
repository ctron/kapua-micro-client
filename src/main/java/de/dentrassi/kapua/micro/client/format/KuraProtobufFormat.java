/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package de.dentrassi.kapua.micro.client.format;

import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.ByteString;

import de.dentrassi.kapua.micro.client.Payload;
import de.dentrassi.kapua.micro.client.internal.kura.payload.KuraPayloadProto.KuraPayload;
import de.dentrassi.kapua.micro.client.internal.kura.payload.KuraPayloadProto.KuraPayload.Builder;
import de.dentrassi.kapua.micro.client.internal.kura.payload.KuraPayloadProto.KuraPayload.KuraMetric;
import de.dentrassi.kapua.micro.client.internal.kura.payload.KuraPayloadProto.KuraPayload.KuraMetric.ValueType;

public class KuraProtobufFormat implements PayloadFormat {

    private static final PayloadFormat INSTANCE = new KuraProtobufFormat();

    public static PayloadFormat defaultInstance() {
        return INSTANCE;
    }

    private KuraProtobufFormat() {
    }

    @Override
    public byte[] encode(final Payload payload) {
        if (payload == null) {
            return null;
        }

        final Builder builder = KuraPayload.newBuilder();

        builder.setTimestamp(payload.getTimestamp());

        for (final Map.Entry<String, Object> entry : payload.getMetrics().entrySet()) {
            final Object value = entry.getValue();

            if (value == null) {
                continue;
            }

            final KuraMetric.Builder metric = KuraPayload.KuraMetric.newBuilder().setName(entry.getKey());

            if (value instanceof String) {
                metric.setType(ValueType.STRING);
                metric.setStringValue((String) value);
            } else if (value instanceof Integer) {
                metric.setType(ValueType.INT32);
                metric.setIntValue((Integer) value);
            } else if (value instanceof Long) {
                metric.setType(ValueType.INT64);
                metric.setLongValue((Long) value);
            } else if (value instanceof Boolean) {
                metric.setType(ValueType.BOOL);
                metric.setBoolValue((Boolean) value);
            } else if (value instanceof Double) {
                metric.setType(ValueType.DOUBLE);
                metric.setDoubleValue((Double) value);
            } else if (value instanceof Float) {
                metric.setType(ValueType.FLOAT);
                metric.setFloatValue((Float) value);
            } else if (value instanceof byte[]) {
                metric.setType(ValueType.BYTES);
                metric.setBytesValue(ByteString.copyFrom((byte[]) value));
            } else {
                throw new IllegalArgumentException("Unsupported data type: " + value.getClass());
            }

            builder.addMetric(metric);
        }
        return builder.build().toByteArray();
    }

    @Override
    public Payload decode(final byte[] buffer) throws Exception {
        if (buffer == null) {
            return null;
        }

        final KuraPayload payload = KuraPayload.parseFrom(buffer);

        final Map<String, Object> metrics = new HashMap<>(payload.getMetricCount());

        for (final KuraMetric metric : payload.getMetricList()) {

            final String name = metric.getName();

            switch (metric.getType()) {
            case STRING:
                metrics.put(name, metric.getStringValue());
                break;
            case INT32:
                metrics.put(name, metric.getIntValue());
                break;
            case INT64:
                metrics.put(name, metric.getLongValue());
                break;
            case BOOL:
                metrics.put(name, metric.getBoolValue());
                break;
            case DOUBLE:
                metrics.put(name, metric.getDoubleValue());
                break;
            case FLOAT:
                metrics.put(name, metric.getFloatValue());
                break;
            case BYTES:
                metrics.put(name, metric.getBytesValue().toByteArray());
                break;
            }
        }

        return new Payload.Builder()
                .from(payload.getTimestamp())
                .metrics(metrics)
                .build();
    }

}
