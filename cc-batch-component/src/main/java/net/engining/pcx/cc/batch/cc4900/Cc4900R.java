package net.engining.pcx.cc.batch.cc4900;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.engining.gm.facility.SystemStatusFacility;
import net.engining.gm.param.model.SystemStatus;
import net.engining.pg.batch.sdk.AbstractKeyBasedReader;

/**
 * SystemStatus对象读取
 * 
 */
@Service
@Scope("step")
public class Cc4900R extends AbstractKeyBasedReader<Integer, SystemStatus> {
	@PersistenceContext
	protected EntityManager em;
	
	@Autowired
	private SystemStatusFacility systemStatusfacility;

	@Override
	protected List<Integer> loadKeys() {
		 List<Integer> key = 	 new ArrayList<Integer>();
		 key.add(Integer.valueOf(1));
	   return key;
	}

	@Override
	protected SystemStatus loadItemByKey(Integer key) {
		
		return systemStatusfacility.getSystemStatus();
	}
}
