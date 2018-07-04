package net.engining.pcx.cc.batch.cc6000;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

import net.engining.pg.parameter.OrganizationContextHolder;


/**
 * 为下面的所有Processor设置Organization上下文
 * @author licj
 *
 */
@Service
public class Cc6000P00OrganizationSetup implements ItemProcessor<Cc6000IAccountInfo, Cc6000IAccountInfo> {

	@Override
	public Cc6000IAccountInfo process(Cc6000IAccountInfo item) throws Exception
	{
		OrganizationContextHolder.setCurrentOrganizationId(item.getCactAccount().getOrg());
		
		return item;
	}
	
}
