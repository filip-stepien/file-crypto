package xyz.cursedman.filecrypto.exceptions;

import lombok.experimental.StandardException;

import java.io.IOException;

@StandardException
public class InvalidEncryptedHeaderException extends IOException {
}
