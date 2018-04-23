package com.karthiksr.streams.demo.storm

import org.apache.storm.Config
import org.apache.storm.LocalCluster
import org.apache.storm.kafka.BrokerHosts
import org.apache.storm.kafka.KafkaSpout
import org.apache.storm.kafka.SpoutConfig
import org.apache.storm.kafka.StringScheme
import org.apache.storm.kafka.ZkHosts
import org.apache.storm.kafka.bolt.KafkaBolt
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector
import org.apache.storm.spout.SchemeAsMultiScheme
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.tuple.Fields

class StormTest {

	TopologyBuilder builder = new TopologyBuilder()

	def setup() {
		BrokerHosts hosts = new ZkHosts('localhost');

		//Creating SpoutConfig Object
		SpoutConfig spoutConfig = new SpoutConfig(hosts, 
		   'test', '','Storm-1')
		
		
		//convert the ByteBuffer to String.
		spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
		
		//Assign SpoutConfig to KafkaSpout.
		KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig)
		builder.setSpout('kafka-spout', kafkaSpout)
		builder.setBolt('transform-bolt',new TransformBolt()).shuffleGrouping('kafka-spout')
		builder.setBolt('sum-bolt',new SumWindowBolt()).fieldsGrouping('transform-bolt',new Fields('name'))
		builder.setBolt('kafka-transformer-bolt', new KafkaTransformerBolt()).shuffleGrouping('sum-bolt')
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "1");
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		
		KafkaBolt bolt = new KafkaBolt()
		.withProducerProperties(props)
		.withTopicSelector(new DefaultTopicSelector('two-second-output'))
		.withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper())
		builder.setBolt("two-second-kafka-bolt", bolt, 8).shuffleGrouping('kafka-transformer-bolt')
		return this
	}
	
	void run() {
		LocalCluster cluster = new LocalCluster()
		Config config = new Config()
		config.setDebug(false)
		config.setNumWorkers(4)
		cluster.submitTopology('KSRTopology', config, builder.createTopology())
		//StormSubmitter.submitTopology('DamTopology',config,builder.createTopology())
		Thread.sleep(1000000)
		  
		//Stop the topology
		  
		cluster.shutdown();
	}

	public static void main(String[] args) {
		new StormTest().setup().run()
	}
}
