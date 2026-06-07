; KeepX — Secure Offline Password Manager Setup Script
; For Inno Setup Compiler (https://jrsoftware.org/isinfo.php)

[Setup]
; App Metadata
AppName=KeepX
AppVersion=1.0.0
AppPublisher=KeepX Team
AppCopyright=Copyright (C) 2026 KeepX Team
DefaultDirName={autopf}\KeepX
DefaultGroupName=KeepX
UninstallDisplayIcon={app}\KeepX.exe
Compression=lzma2/max
SolidCompression=yes
OutputDir=target
OutputBaseFilename=KeepX_Setup

; Architecture config
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; The main launcher executable built by launch4j
Source: "target\KeepX.exe"; DestDir: "{app}"; Flags: ignoreversion

; The shaded runnable JAR file containing all classes and dependencies
Source: "target\KeepX.jar"; DestDir: "{app}"; Flags: ignoreversion

; Optional: Include bundled JRE folder next to the EXE if present
; Uncomment the line below when you place a local 'jre' folder inside the project root
; Source: "jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\KeepX"; Filename: "{app}\KeepX.exe"
Name: "{autoprograms}\KeepX"; Filename: "{app}\KeepX.exe"
Name: "{autodesktop}\KeepX"; Filename: "{app}\KeepX.exe"; Tasks: desktopicon

[Run]
Description: "{cm:LaunchProgram,KeepX}"; Filename: "{app}\KeepX.exe"; Flags: nowait postinstall skipifsilent
