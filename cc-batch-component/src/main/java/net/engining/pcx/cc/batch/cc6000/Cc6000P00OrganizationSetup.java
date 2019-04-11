package net.engining.pcx.cc.batch.cc6000;

import net.engining.pg.support.core.context.OrganizationContextHolder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;



/**
 * 为下面的所有Processor设置Organization上下文
 * @author licj
 *
 */
@Service
@StepScope
public class Cc6000P00OrganizationSetup implements ItemProcessor<Cc6000IAccountInfo, Cc6000IAccountInfo> {

	@Override
	public Cc6000IAccountInfo process(Cc6000IAccountInfo item) throws Exception
	{
		OrganizationContextHolder.setCurrentOrganizationId(item.getCactAccount().getOrg());
		
		return item;
	}
	
}
