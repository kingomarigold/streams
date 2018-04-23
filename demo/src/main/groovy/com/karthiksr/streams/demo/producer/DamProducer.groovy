package com.karthiksr.streams.demo.producer

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord

class DamProducer {

	public static void main(String[] args) {
		Properties props = new Properties()
		
		props.put("bootstrap.servers", 'localhost:9092')
		props.put("acks", 'all')
		props.put("retries", 0)
		props.put("batch.size", 16384)
		props.put("linger.ms", 1)
		props.put("buffer.memory", 33554432)
		props.put("key.serializer",
		   "org.apache.kafka.common.serialization.StringSerializer")
		props.put("value.serializer",
		   "org.apache.kafka.common.serialization.StringSerializer")
		
		Producer<String, String> producer = new KafkaProducer<String, String>(props)
		
		Long millis = System.currentTimeMillis()
		Random random = new Random()
		for (int i = 0 ; i < 100; ++i) {
			for (int j = 0; j < 1000; ++j) {
				producer.send(new ProducerRecord('test',"Mettur,${millis+(i*1000+j)},${random.nextInt(500)}".toString()))
				producer.send(new ProducerRecord('test',"KRS,${millis+(i*1000+j)},${random.nextInt(500)}".toString()))
			}
		}
	}
}
