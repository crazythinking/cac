package net.engining.pcx.cc.batch.cc4900;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.engining.gm.infrastructure.enums.SystemStatusType;
import net.engining.gm.param.model.SystemStatus;
import net.engining.pg.parameter.ParameterFacility;

/**
 * 更新系统状态
 */
@Service
@Scope("step")
public class Cc4900P00 implements ItemProcessor<SystemStatus, Object> {
	 
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private ParameterFacility parameterXStreamFacility;

	@Override
	public Object process(SystemStatus systemStatus) throws Exception {
		
		systemStatus.systemStatus = SystemStatusType.N;
		try {
			parameterXStreamFacility.updateParameter(ParameterFacility.UNIQUE_PARAM_KEY, systemStatus);
		} catch (Exception e) {
			throw new RuntimeException("参数不存在");
		}
		    return null;
	}
}
