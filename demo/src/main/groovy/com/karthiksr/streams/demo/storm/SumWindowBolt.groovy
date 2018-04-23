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
		Long total = 0
		def name =''
		def startTime = 0, endTime=0
		inputWindow.get().each { Tuple value ->
			total += value.getLongByField('volume')
			def timeStamp = value.getLongByField('timeStamp')
			if (timeStamp < startTime || !startTime) {
				startTime = timeStamp
			}
			if (timeStamp > endTime) {
				endTime = timeStamp
			}
			if (!name) {
				name = value.getStringByField('name')
			}
		}
		outputCollector.emit(new Values(name,total,startTime,endTime))
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields('name','totalVolume','startTime','endTime'))
	}	
}
