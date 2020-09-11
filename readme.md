# react-native-biopass-updated

Store a password behind biometric authentication.

Currently only supports the Android platform.

This package uses androidx.biometric.BiometricPrompt with fallback to android.hardware.fingerprint.FingerprintManager for Android versions below Android P and can only be used in projects using googles androidx library. RN versions above 0.60 use androidx library by default.

With this I have also implemented device passcode fallback support in case of biometric authentication failure. While its easy to implement passcode fallback for BiometricPrompt its a bit tricky for FingerPrintManager and you will have to update the MainActivity.java of your application to do this. Follow the steps below in #MainActivity in ## Usage to do this.

## Installation

```sh
npm install --save react-native-biopass-updated
react-native link react-native-biopass-updated
```

## Usage

```js
import BioPass from 'react-native-biopass-updated'

// Store a password for future retreival
BioPass.store("secret", password)
  .then(() => console.log(`Password stored!`))
  .catch((err) => console.log(`Failed to store password: ${err}`)

// Retreive a stored password (will trigger Fingerprint / TouchID / FaceID prompt)
BioPass.retreive("Give us the secret password!")
  .then((password) => console.log(`The password was: ${password}`))
  .catch((err) => console.log(`Failed to retreive password: ${err}`)

// Delete the stored password
BioPass.delete()
  .then(() => console.log(`Password deleted!`))
  .catch((err) => console.log(`Failed to delete password: ${err}`)
```

# MainActivity
import android.app.Activity;
import com.reactlibrary.RNBioPassPackage;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.ReactRootView;
import com.facebook.react.ReactInstanceManager;

public class MainActivity extends AppCompatActivity {
    private ReactInstanceManager mReactInstanceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Instantiate ReactInstanceManager
        this.mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setCurrentActivity(this)
                .setBundleAssetName("index.android.bundle")
                .addPackage(new RNBioPassPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Activity activity = this;
        mReactInstanceManager.onActivityResult(activity, requestCode, resultCode, data);
    }
}


## Related Projects

- [BioPass for iOS](https://github.com/LinusU/BioPass)

## Manual installation

### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-biopass-updated` and add `RNBioPass.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNBioPass.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
    - Add `import com.reactlibrary.RNBioPassPackage;` to the imports at the top of the file
    - Add `new RNBioPassPackage()` to the list returned by the `getPackages()` method
1. Append the following lines to `android/settings.gradle`:

    ```gradle
    include ':react-native-biopass-updated'
    project(':react-native-biopass-updated').projectDir = new File(rootProject.projectDir '../node_modules/react-native-biopass-updated/android')
    ```

1. Insert the following lines inside the dependencies block in `android/app/build.gradle`:

    ```gradle
      compile project(':react-native-biopass-updated')
    ```

#### Windows

[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNBioPass.sln` in `node_modules/react-native-biopass-updated/windows/RNBioPass.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
    - Add `using Bio.Pass.RNBioPass;` to the usings at the top of the file
    - Add `new RNBioPassPackage()` to the `List<IReactPackage>` returned by the `Packages` method
