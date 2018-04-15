package com.karthiksr.streams.demo

import java.util.concurrent.CountDownLatch

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.Consumed
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Aggregator
import org.apache.kafka.streams.kstream.Initializer
import org.apache.kafka.streams.kstream.KeyValueMapper
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.Serialized
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.kstream.Windowed
import org.apache.kafka.streams.kstream.internals.WindowedDeserializer
import org.apache.kafka.streams.kstream.internals.WindowedSerializer
import org.apache.kafka.streams.processor.TimestampExtractor

import com.fasterxml.jackson.databind.ObjectMapper

class Pipe {

	Properties props = new Properties()
	StreamsBuilder builder = new StreamsBuilder()

	def init() {
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-pipe")
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass())
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass())
	}

	def run() {
		Serde<String> keySerde = Serdes.String()
		def customValueSerializer = [
			configure: {a,b->},
			serialize: {topic,data->
				new ObjectMapper().writeValueAsBytes(data)
			}
		] as Serializer
		def customValueFromStringDeserializer = [
			configure: {a,b->},
			deserialize: {topic,data->
				def retVal = new CustomValue()
				if (data) {
					def myValues =  new String(data, "UTF-8").split(',')
					if (myValues.size() == 3) {
						retVal = new CustomValue(dam:myValues[0],millis:myValues[1].asType(Long.class),
						cubicFeet:myValues[2].asType(Long.class))
					}
				}
				retVal
			}
		] as Deserializer
		def customValueDeserializer = [
			configure: {a,b->},
			deserialize: {topic,data->
				def retVal = new CustomValue()
				if (data) {
					try {
						def myData = new String(data, "UTF-8")
						retVal = new ObjectMapper().readValue(myData,CustomValue.class)
					}
					catch(Throwable t) {
						
					}
				}
				retVal
			}
		] as Deserializer
		def valueSerde = new CustomValueSerde(customValueSerializer,customValueFromStringDeserializer)
		def mappingSerde = new CustomValueSerde(customValueSerializer,customValueDeserializer)
		def consumed = Consumed.with(keySerde,valueSerde).withTimestampExtractor([
			extract:{record,time-> 
				def retVal = time
				if (record.value() && record.value() instanceof CustomValue) {
					retVal = record.value().millis
				}
				retVal
			}
		] as TimestampExtractor)
		WindowedSerializer<String> windowedSerializer = new WindowedSerializer<>(new StringSerializer())
		WindowedDeserializer<String> windowedDeserializer = new WindowedDeserializer<>( new StringDeserializer())
		Serde<Windowed<String>> windowedSerde = Serdes.serdeFrom(windowedSerializer,windowedDeserializer)
		builder.stream("test",consumed)
				.map([apply:{key,value->
						new KeyValue<String,CustomValue>(value.dam,value)
					}] as KeyValueMapper)
		.groupByKey(Serialized.with(keySerde,mappingSerde))
		.windowedBy(TimeWindows.of(2*1000))
		.aggregate([apply:{ new CustomValue()}] as Initializer,
			[apply:{key,value,agg-> 
				new CustomValue(dam:key,cubicFeet:agg.cubicFeet + value.cubicFeet)}] as Aggregator,
			Materialized.with(keySerde,mappingSerde))
		.toStream()
				.to('streams-pipe-output',Produced.with(windowedSerde,mappingSerde))

		Topology topology = builder.build()
		println topology.describe()
		KafkaStreams streams = new KafkaStreams(topology, props)

		CountDownLatch latch = new CountDownLatch(1)

		// attach shutdown handler to catch control-c
		Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
					@Override
					public void run() {
						streams.close()
						latch.countDown()
					}
				});

		try {
			streams.start()
			latch.await()
		} catch (Throwable e) {
			System.exit(1);
		}
		System.exit(0);
	}
}
