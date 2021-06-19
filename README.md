# biometric-protection

Biometric protection is a simple app that implements [Android Biometric library](https://developer.android.com/jetpack/androidx/releases/biometric).

One method of protecting sensitive information which makes it possible authorize the user with fingerprint, iris, face-id and any others biometric credential to access protected data.
This library uses the default unlock method enabled in the device settings.

<img src="https://img.shields.io/badge/API-23%2B-orange" style="max-width:100%;" alt="API" data-canonical-src="https://img.shields.io/badge/API-23%2B-orange" style="max-width:100%;">

# App structure
The application is composed by two modules:
- **App** module include a simple UI to encrypt and decrypt a simple text. In order to decrypt and access to protected data is shown a biometric authentication dialog. After the user authenticates successfully using a biometric prompt, it is possible to perform a cryptographic operation and encrypt a text string.
- **Biometric protection** module consist of the business logic of Biometric library. In this module is implemented the way to further protect sensitive information and incorporate cryptography into biometric authentication workflow using an instance of CryptoObject. The biometric module use Cipher as cryptographic object.

# Third-party libraries
**Koin**  
Koin is a framework for the dependency injection. I have chosen to use this framework because I think is more simple than Dagger2. Avoid writing many classes, reducing boilerplate and it's easier to understand.
Link: https://github.com/InsertKoinIO/koin

**RxJava**  
It is useful to do async operations and react to events.
Link: https://github.com/ReactiveX/RxJava