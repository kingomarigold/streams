package com.karthiksr.streams.demo.storm

import org.apache.storm.task.OutputCollector
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.tuple.Fields
import org.apache.storm.tuple.Values
import org.apache.storm.windowing.TupleWindow
import org.apache.storm.tuple.Tuple

class SumWindowBolt extends BaseWindowedBolt {
	
	OutputCollector outputCollector
	
	SumWindowBolt() {
		this.withTimestampExtractor(new StormTimestampExtractor()).withTumblingWindow(Duration.of(2000))
	}
	
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		outputCollector = collector
	}

	@Override
	public void execute(TupleWindow inputWindow) {
		def group = [:]
		inputWindow.get().each { Tuple value ->
			def name = value.getStringByField('name')
			def myValue = group.get(name,[total:0,startTime:0,endTime:0])
			myValue.total += value.getLongByField('volume')
			def timeStamp = value.getLongByField('timeStamp')
			if (timeStamp < myValue.startTime || !myValue.startTime) {
				myValue.startTime = timeStamp
			}
			if (timeStamp > myValue.endTime) {
				myValue.endTime = timeStamp
			}
		}
		group.each {key,value->
			outputCollector.emit(new Values(key,value.total,value.startTime,value.endTime))
		}
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields('name','totalVolume','startTime','endTime'))
	}	
}
