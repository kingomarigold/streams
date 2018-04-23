package com.karthiksr.streams.demo.storm

import org.apache.storm.task.OutputCollector
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.IRichBolt
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.tuple.Fields
import org.apache.storm.tuple.Tuple
import org.apache.storm.tuple.Values

class TransformBolt implements IRichBolt {
	
	OutputCollector outputCollector

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		outputCollector = collector
	}

	@Override
	public void execute(Tuple input) {
		def splitVals = input.getString(0).split(',')
		outputCollector.emit(new Values(splitVals[0],splitVals[1].asType(Long.class),
			splitVals[2].asType(Long.class)))
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields('name','timeStamp','volume'))
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null
	}

}
