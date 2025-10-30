package xyz.cursedman.filecrypto.cryptors.CaesarCryptor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;

@Setter
@Getter
@Builder
public class CaesarCryptorKey implements CryptorKey {
    int shift;
}
