package com.karthiksr.streams.demo.storm

import org.apache.storm.task.OutputCollector
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.IRichBolt
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.tuple.Fields
import org.apache.storm.tuple.Tuple
import org.apache.storm.tuple.Values

class KafkaTransformerBolt implements IRichBolt {
	
	OutputCollector outputCollector

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		outputCollector = collector
	}

	@Override
	public void execute(Tuple input) {
		outputCollector.emit(new Values(input.getStringByField('name'),
			"{\"name\":\"${input.getStringByField('name')}\",\"startTime\":\"${input.getLongByField('startTime')}\",\"endTime\":\"${input.getLongByField('endTime')}\",\"totalVolume\":\"${input.getLongByField('totalVolume')}\"}".toString()))
	}

	@Override
	public void cleanup() {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields('key','message'))
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null
	}

}
