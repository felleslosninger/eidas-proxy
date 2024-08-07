/*
 * Copyright (c) 2023 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package eu.eidas.auth.engine.configuration.dom;

// START: Added code by ID-porten team
import java.util.regex.Pattern;
import static org.apache.commons.lang.StringUtils.trimToNull;
// END: Added code by ID-porten team

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.core.eidas.spec.EidasSignatureConstants;
import eu.eidas.auth.engine.core.impl.CertificateValidator;
import eu.eidas.auth.engine.core.impl.WhiteListConfigurator;

import javax.annotation.Nullable;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.trim;


/**
 * KeyStore-based SignatureConfigurator.
 *
 * @since 1.1
 */
public final class KeyStoreSignatureConfigurator {

    private String tryConfigurationKeyPreferPrefix(Map<String, String> properties, SignatureKey signatureKey, String propPrefix) {
        final String prefixedSignatureKey = propPrefix + signatureKey.getKey();
        if (properties.containsKey(prefixedSignatureKey)) {
            return prefixedSignatureKey;
        } else {
            return signatureKey.getKey();
        }
    }

    public SignatureConfiguration getSignatureConfiguration(Map<String, String> properties, @Nullable String defaultPath)
            throws ProtocolEngineConfigurationException {
        boolean checkedValidityPeriod = CertificateValidator.isCheckedValidityPeriod(properties);
        boolean disallowedSelfSignedCertificate = CertificateValidator.isDisallowedSelfSignedCertificate(properties);
        boolean responseSignAssertions = Boolean.parseBoolean(trim(SignatureKey.RESPONSE_SIGN_ASSERTIONS.getAsString(properties)));
        boolean requestSignWithKey =     Boolean.parseBoolean(trim(SignatureKey.REQUEST_SIGN_WITH_KEY_VALUE.getAsString(properties)));
        boolean responseSignWithKey =    Boolean.parseBoolean(trim(SignatureKey.RESPONSE_SIGN_WITH_KEY_VALUE.getAsString(properties)));

        final String signatureAlgorithm =         SignatureKey.SIGNATURE_ALGORITHM.getAsString(properties);
        final String metadataSignatureAlgorithm = SignatureKey.METADATA_SIGNATURE_ALGORITHM.getAsString(properties);
        final String digestMethodAlgorithm =      SignatureKey.DIGEST_METHOD_ALGORITHM.getAsString(properties);

        // START: Changed code by ID-porten team
        final ImmutableSet<String> allowedSignatureAlgorithmWhitelist = verifySigningAlgorithmsWhitelisted(
                EidasSignatureConstants.DEFAULT_SIGNATURE_ALGORITHM_WHITE_LIST,
                SignatureKey.SIGNATURE_ALGORITHM_WHITE_LIST.getAsString(properties)
        );
        // END: Changed code by ID-porten team

        final String signatureAlgorithmWhiteListStr = String.join(EIDASValues.SEMICOLON.toString(), allowedSignatureAlgorithmWhitelist);

        final ImmutableSet<String> digestMethodAlgorithmWhiteList = WhiteListConfigurator.getAllowedAlgorithms(
                EidasSignatureConstants.DEFAULT_DIGEST_ALGORITHM_WHITE_LIST,
                SignatureKey.DIGEST_METHOD_ALGORITHM_WHITELIST.getAsString(properties)
        );

        final ArrayList<KeyStoreContent> keystoreContentList = new ArrayList<>();
        keystoreContentList.addAll(fetchLegacyConfiguration(properties, defaultPath));
        int i = 1;
        while (properties.containsKey(i + "." + KeyStoreKey.KEYSTORE_TYPE.getKey())) {
            final KeyStoreContent keyStoreContent = new KeyStoreConfigurator(properties, getNumberPrefixConfigurationKeys(i), defaultPath).loadKeyStoreContent();
            keystoreContentList.add(keyStoreContent);
            i++;
        }

        final KeyContainer keyStoreContent = new ListKeystoreContent(keystoreContentList);
        final String serialNumber = SignatureKey.SERIAL_NUMBER.getAsString(properties);
        final String issuer =       SignatureKey.ISSUER.getAsString(properties);

        final KeyContainer trustStoreContent = new ListKeystoreContent(keystoreContentList)
                .subset(KeyStoreContent.KeystorePurpose.TRUSTSTORE);
        final ImmutableSet<X509Certificate> trustedCertificates = trustStoreContent.getCertificates();
        final KeyStore.PrivateKeyEntry signatureKeyAndCertificate = keyStoreContent.getMatchingPrivateKeyEntry(serialNumber, issuer);


        final String metadataPrefix = SignatureKey.METADATA_PREFIX.getKey();
        final String metadataSerialNumberKey = tryConfigurationKeyPreferPrefix(properties, SignatureKey.SERIAL_NUMBER, metadataPrefix);
        final String metadataSerialNumber = properties.get(metadataSerialNumberKey);
        final String metadataIssuerKey = tryConfigurationKeyPreferPrefix(properties, SignatureKey.ISSUER, metadataPrefix);
        final String metadataIssuer = properties.get(metadataIssuerKey);

        final ImmutableSet<X509Certificate> metadataKeystoreCertificates = keyStoreContent.getCertificates();
        final KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate = keyStoreContent.getMatchingPrivateKeyEntry(metadataSerialNumber, metadataIssuer);


        SignatureConfiguration.Builder signatureConfigurationBuilder = new SignatureConfiguration.Builder();
        signatureConfigurationBuilder.setDigestAlgorithm(digestMethodAlgorithm);
        signatureConfigurationBuilder.setDigestMethodAlgorithmWhiteList(digestMethodAlgorithmWhiteList);
        signatureConfigurationBuilder.setSignatureAlgorithm(signatureAlgorithm);
        signatureConfigurationBuilder.setSignatureAlgorithmWhiteList(signatureAlgorithmWhiteListStr);
        signatureConfigurationBuilder.setSignatureKeyAndCertificate(signatureKeyAndCertificate);
        signatureConfigurationBuilder.setTrustedCertificates(trustedCertificates);
        signatureConfigurationBuilder.setMetadataSignatureAlgorithm(metadataSignatureAlgorithm);
        signatureConfigurationBuilder.setMetadataSigningKeyAndCertificate(metadataSigningKeyAndCertificate);
        signatureConfigurationBuilder.setMetadataKeystoreCertificates(metadataKeystoreCertificates);
        signatureConfigurationBuilder.setCheckedValidityPeriod(checkedValidityPeriod);
        signatureConfigurationBuilder.setDisallowedSelfSignedCertificate(disallowedSelfSignedCertificate);
        signatureConfigurationBuilder.setRequestSignWithKey(requestSignWithKey);
        signatureConfigurationBuilder.setResponseSignWithKey(responseSignWithKey);
        signatureConfigurationBuilder.setResponseSignAssertions(responseSignAssertions);

        return signatureConfigurationBuilder.build();

    }

    private List<KeyStoreContent> fetchLegacyConfiguration(Map<String, String> properties, String defaultPath) throws ProtocolEngineConfigurationException {
        ArrayList<KeyStoreContent> legacyKeyStores = new ArrayList<>();

        if(properties.containsKey(KeyStoreConfigurator.DEFAULT_KEYSTORE_CONFIGURATION_KEYS.getKeyStoreTypeConfigurationKey())) {
            final KeyStoreContent defaultKeyStoreContent = new KeyStoreConfigurator(properties, defaultPath).loadKeyStoreContent();
            legacyKeyStores.add(defaultKeyStoreContent);
        }

        final String metadataPrefix = SignatureKey.METADATA_PREFIX.getKey();
        final KeyStoreConfigurator.KeyStoreConfigurationKeys keyStoreConfigurationKeys = KeyStoreConfigurator.prefixPostfixConfigurationKeys(metadataPrefix, "");
        if (properties.containsKey(keyStoreConfigurationKeys.getKeyStoreTypeConfigurationKey())) {
            final KeyStoreContent metadataKeyStoreContent = new KeyStoreConfigurator(properties, keyStoreConfigurationKeys, defaultPath).loadKeyStoreContent();
            legacyKeyStores.add(metadataKeyStoreContent);
        }
        return legacyKeyStores;
    }

    private static KeyStoreConfigurator.KeyStoreConfigurationKeys getNumberPrefixConfigurationKeys(int prefixCounter) {
        return KeyStoreConfigurator.prefixPostfixConfigurationKeys(prefixCounter + ".","");
    }

    /**
     * Added method by ID-porten team.
     * Also added corrsponding imports of StringUtils.trimToNull and Pattern.
     *
     * @param defaultWhiteList
     * @param myAlgs
     * @return
     */
    private static ImmutableSet<String> verifySigningAlgorithmsWhitelisted(ImmutableSet<String> defaultWhiteList, String myAlgs) {
        if(myAlgs == null || myAlgs.isEmpty()){
            return defaultWhiteList;
        }
        Pattern WHITE_LIST_SPLITTER = Pattern.compile("[;,]");
        String[] wlAlgorithms = WHITE_LIST_SPLITTER.split(myAlgs);
        ImmutableSet.Builder<String>  tmp = ImmutableSet.builder();
        for (String algValue : wlAlgorithms) {
            String alg = trimToNull(algValue);
            tmp.add(alg);
        }
        return tmp.build();
    }
}
