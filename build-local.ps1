param(
    [string]$Sdk = "$env:LOCALAPPDATA\Android\Sdk",
    [string]$Jdk = "C:\Program Files\Android\Android Studio\jbr"
)
$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$buildRoot = Join-Path $projectRoot 'app/build/direct'
$bt = Join-Path $Sdk 'build-tools/36.1.0'
$androidJar = Join-Path $Sdk 'platforms/android-36.1/android.jar'
$env:JAVA_HOME = $Jdk
function Run-Tool([string]$Tool, [string[]]$ToolArgs) {
    & $Tool @ToolArgs
    if ($LASTEXITCODE -ne 0) { throw "$Tool failed: $LASTEXITCODE" }
}
foreach ($folder in @($buildRoot,"$buildRoot/main-classes","$buildRoot/dex")) {
    [System.IO.Directory]::CreateDirectory($folder) | Out-Null
}
# The direct SDK build is a fallback for hosts where Gradle's daemon cannot connect.
$manifest = [System.IO.File]::ReadAllText("$projectRoot/app/src/main/AndroidManifest.xml")
$manifest = $manifest.Replace('<manifest ', '<manifest package="hu.wortwerk" android:versionCode="2" android:versionName="1.1.0" ')
$manifest = $manifest.Replace('<application ', '<uses-sdk android:minSdkVersion="26" android:targetSdkVersion="36"/><application android:debuggable="true" ')
[System.IO.File]::WriteAllText("$buildRoot/AndroidManifest.xml",$manifest)
Run-Tool "$bt/aapt2.exe" @('compile','--dir',"$projectRoot/app/src/main/res",'-o',"$buildRoot/resources.zip")
Run-Tool "$bt/aapt2.exe" @('link','-o',"$buildRoot/unsigned.apk",'--manifest',"$buildRoot/AndroidManifest.xml",'-I',$androidJar,'-A',"$projectRoot/app/src/main/assets","$buildRoot/resources.zip")
$sources = @(Get-ChildItem "$projectRoot/app/src/main/java" -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
Run-Tool "$Jdk/bin/javac.exe" (@('-encoding','UTF-8','-source','17','-target','17','-classpath',$androidJar,'-d',"$buildRoot/main-classes") + $sources)
Run-Tool "$Jdk/bin/jar.exe" @('--create','--file',"$buildRoot/main-classes.jar",'-C',"$buildRoot/main-classes",'.')
Run-Tool "$bt/d8.bat" @('--lib',$androidJar,'--min-api','26','--output',"$buildRoot/dex","$buildRoot/main-classes.jar")
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::Open("$buildRoot/unsigned.apk",'Update')
try {
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive,"$buildRoot/dex/classes.dex",'classes.dex') | Out-Null
} finally { $archive.Dispose() }
Run-Tool "$bt/zipalign.exe" @('-f','4',"$buildRoot/unsigned.apk","$buildRoot/aligned.apk")
if (!(Test-Path "$buildRoot/debug.keystore")) {
    Run-Tool "$Jdk/bin/keytool.exe" @('-genkeypair','-keystore',"$buildRoot/debug.keystore",'-storepass','android','-keypass','android','-alias','androiddebugkey','-dname','CN=Android Debug,O=Android,C=US','-keyalg','RSA','-keysize','2048','-validity','10000')
}
Run-Tool "$bt/apksigner.bat" @('sign','--ks',"$buildRoot/debug.keystore",'--ks-pass','pass:android','--key-pass','pass:android','--out',"$buildRoot/WortWerk-debug.apk","$buildRoot/aligned.apk")
Run-Tool "$bt/apksigner.bat" @('verify','--verbose',"$buildRoot/WortWerk-debug.apk")
Write-Output "APK: $buildRoot/WortWerk-debug.apk"
