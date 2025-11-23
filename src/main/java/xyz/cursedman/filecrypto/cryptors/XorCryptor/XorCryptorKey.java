package xyz.cursedman.filecrypto.cryptors.XorCryptor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;

@Getter
@Setter
@Builder
public class XorCryptorKey implements CryptorKey {
    private byte[] key; // can be any length
}
