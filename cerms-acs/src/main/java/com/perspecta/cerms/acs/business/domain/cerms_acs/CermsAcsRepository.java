package com.perspecta.cerms.acs.business.domain.cerms_acs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CermsAcsRepository extends JpaRepository<CermsAcs, Long> {
	CermsAcs findBySerialNumber (String serialNumber);
}
