package net.engining.pcx.cc.process.service.param;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.InterestTable;
import net.engining.pcx.cc.param.model.SubAcct;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.parameter.entity.model.ParameterSeqence;

@Service
public class CcParamComparatorService {

	@PersistenceContext
	private EntityManager em;

	@Autowired
	ParameterFacility parameterFacility;

	public InterestTable jdugeNewInterestTable(InterestTable newOne) {
		Map<String, InterestTable> interestTablesMap = parameterFacility.getParameterMap(InterestTable.class);

		for (String key : interestTablesMap.keySet()) {
			InterestTable oldOne = interestTablesMap.get(key);
			// 只要找到一个相同的参数就返回
			if (oldOne.compareTo(newOne) == 0) {
				return oldOne;
			}
		}

		// 没有找到的情况，需要增加参数到系统
		ParameterSeqence ParameterSeqence = em.find(ParameterSeqence.class, InterestTable.class.getCanonicalName());
		ParameterSeqence.setParamSeq(ParameterSeqence.getParamSeq() + 1);
		String parameter ="INT" + StringUtils.leftPad(ParameterSeqence.getParamSeq().toString(), 10, "0");
		newOne.interestCode=parameter ;
		parameterFacility.addParameter(parameter, newOne);

		return newOne;
	}

	public SubAcct jdugeNewSubAcct(SubAcct newOne) {
		Map<String, SubAcct> subAcctsMap = parameterFacility.getParameterMap(SubAcct.class);

		for (String key : subAcctsMap.keySet()) {
			SubAcct oldOne = subAcctsMap.get(key);
			// 只要找到一个相同的参数就返回
			if (oldOne.compareTo(newOne) == 0) {
				return oldOne;
			}
		}
		
		// 没有找到的情况，需要增加参数到系统
		ParameterSeqence ParameterSeqence = em.find(ParameterSeqence.class, SubAcct.class.getCanonicalName());
		ParameterSeqence.setParamSeq(ParameterSeqence.getParamSeq() + 1);
		String parameter ="SUB" + StringUtils.leftPad(ParameterSeqence.getParamSeq().toString(), 10, "0");
		newOne.subAcctId=parameter ;
		parameterFacility.addParameter(parameter, newOne);
		
		return newOne;
	}

	public Account jdugeNewAccount(Account newOne) {
		Map<String, Account> subAcctsMap = parameterFacility.getParameterMap(Account.class);

		for (String key : subAcctsMap.keySet()) {
			Account oldOne = subAcctsMap.get(key);
			// 只要找到一个相同的参数就返回
			if (oldOne.compareTo(newOne) == 0) {
				return oldOne;
			}
		}
		
		// 没有找到的情况，需要增加参数到系统
		ParameterSeqence ParameterSeqence = em.find(ParameterSeqence.class, Account.class.getCanonicalName());
		ParameterSeqence.setParamSeq(ParameterSeqence.getParamSeq() + 1);
		String parameter ="ACC" + StringUtils.leftPad(ParameterSeqence.getParamSeq().toString(), 10, "0");
		newOne.paramId=parameter;
		parameterFacility.addParameter(parameter, newOne);
		
		return newOne;
	}
}
