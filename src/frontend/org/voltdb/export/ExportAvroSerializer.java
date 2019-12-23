/* This file is part of VoltDB.
 * Copyright (C) 2008-2019 VoltDB Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.voltdb.export;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.avro.generic.GenericRecord;
import org.voltdb.VoltDB;
import org.voltdb.compiler.deploymentfile.DeploymentType;
import org.voltdb.exportclient.ExportRow;
import org.voltdb.exportclient.decode.AvroDecoder;

import java.util.HashMap;
import java.util.Map;

public class ExportAvroSerializer {
    private final AvroDecoder m_decoder;
    private final KafkaAvroSerializer m_serializer;
    private KafkaAvroSerializerConfig m_config;

    public ExportAvroSerializer() {
        m_decoder = new AvroDecoder.Builder().build();
        m_serializer = new KafkaAvroSerializer();
    }

    public byte[] serialize(ExportRow rd, String topic) {
        GenericRecord avroRecord = (GenericRecord) m_decoder.decode(rd.generation, rd.tableName, rd.types, rd.names,
                null, rd.values);
        byte[] serializedData = m_serializer.serialize(topic, null, avroRecord);
        return serializedData;
    }

    public KafkaAvroSerializerConfig buildConfig() {
        Map<String, String> configMap = new HashMap<>();
        DeploymentType deploymentType = VoltDB.instance().getCatalogContext().getDeployment();
        if (deploymentType.getSchemaRegistryUrl() != null) {
            configMap.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, deploymentType.getSchemaRegistryUrl());
        }
        return new KafkaAvroSerializerConfig(configMap);
    }
}
