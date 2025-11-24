package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import javax.annotation.PostConstruct;

/**
 * Service for mapping prison names to email addresses.
 * Reads prison email addresses from JSON data stored in Azure Key Vault.
 * 
 * <p>The JSON data is injected via the IA_DET_PRISON_EMAIL_MAPPINGS environment variable
 * and should have the following format:
 * 
 * <p>{
 *   "prisonEmailMappings": {
 *     "Addiewell": "adcourts@example.com",
 *     "Belmarsh": "belmarsh@example.com",
 *     "Birmingham": "birmingham@example.com"
 *   }
 * }
 */
@Service
public class PrisonEmailMappingService {

    private static final Logger log = LoggerFactory.getLogger(PrisonEmailMappingService.class);
    
    private final String prisonEmailAddresses;
    private final ObjectMapper objectMapper;
    private final Map<String, String> prisonEmailCache = new HashMap<>();

    public PrisonEmailMappingService(
        @Value("${prison.email.mappings:}") String prisonEmailAddresses
    ) {
        this.prisonEmailAddresses = prisonEmailAddresses;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        loadPrisonEmails();
    }

    /**
     * Loads prison email addresses from JSON data into cache.
     * Expects JSON format: {"prisonEmailMappings": {"Prison Name": "email@example.com"}}
     */
    private void loadPrisonEmails() {
        if (!StringUtils.hasText(prisonEmailAddresses)) {
            log.warn("No prison email addresses configured. IA_DET_PRISON_EMAIL_MAPPINGS environment variable is empty.");
            return;
        }

        try {
            log.info("Loading prison email mappings from vault");
            
            // Parse JSON structure: {"prisonEmailMappings": {...}}
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
            Map<String, Object> jsonData = objectMapper.readValue(prisonEmailAddresses, typeRef);
            
            Object mappingsObj = jsonData.get("prisonEmailMappings");
            if (mappingsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> mappings = (Map<String, String>) mappingsObj;
                
                prisonEmailCache.clear();
                prisonEmailCache.putAll(mappings);
                
                log.info("Successfully loaded {} prison email mappings", prisonEmailCache.size());
                
                // Log all prison email addresses for verification
                log.info("Prison email mappings loaded: {}", prisonEmailCache);
                
                if (log.isDebugEnabled()) {
                    log.debug("Loaded prison mappings: {}", prisonEmailCache.keySet());
                }
            } else {
                log.error("Invalid JSON structure: 'prisonEmailMappings' should be an object");
            }
            
        } catch (Exception e) {
            log.error("Failed to load prison email mappings from JSON: {}", e.getMessage(), e);
            prisonEmailCache.clear();
        }
    }

    /**
     * Gets the email address for a specific prison.
     * 
     * @param prisonName The name of the prison (e.g., "Addiewell", "Askham Grange")
     * @return Optional containing the email address if found, empty otherwise
     */
    public Optional<String> getPrisonEmail(String prisonName) {
        if (prisonName == null || prisonName.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.ofNullable(prisonEmailCache.get(prisonName.trim()));
    }

    /**
     * Checks if a prison is supported (has an email mapping).
     * 
     * @param prisonName The name of the prison
     * @return true if the prison is supported, false otherwise
     */
    public boolean isPrisonSupported(String prisonName) {
        return getPrisonEmail(prisonName).isPresent();
    }

    /**
     * Gets all prison email mappings.
     * 
     * @return Map of prison names to email addresses
     */
    public Map<String, String> getAllPrisonEmails() {
        return new HashMap<>(prisonEmailCache);
    }

    /**
     * Gets all supported prison names.
     * 
     * @return Set of prison names that have email mappings
     */
    public Set<String> getSupportedPrisons() {
        return prisonEmailCache.keySet();
    }

} 