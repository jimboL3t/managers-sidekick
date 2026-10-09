/* SPDX-License-Identifier: GPL-3.0-only
 * Copyright (C) 2026 Dimitrios Diamantis
 * GUI launcher using Windows system APIs only; no external C runtime.
 */
#define UNICODE
#define _UNICODE
#include <windows.h>
#define CAP 32768
static WCHAR directory[CAP], java[CAP], jar[CAP], command[CAP];
static void fail(const WCHAR *message) {
    MessageBoxW(NULL, message, L"Manager's Sidekick", MB_OK | MB_ICONERROR);
    ExitProcess(1);
}
static void append(WCHAR *to, const WCHAR *from) {
    if (lstrlenW(to) + lstrlenW(from) >= CAP)
        fail(L"The application path is too long. Move the folder to a shorter path.");
    lstrcatW(to, from);
}
void mainCRTStartup(void) {
    DWORD length = GetModuleFileNameW(NULL, directory, CAP);
    if (!length || length >= CAP) fail(L"Cannot locate the application folder.");
    while (length && directory[length - 1] != L'\\') --length;
    if (!length) fail(L"Cannot locate the application folder.");
    directory[length - 1] = 0;
    append(java, directory); append(java, L"\\runtime\\bin\\javaw.exe");
    append(jar, directory); append(jar, L"\\managers-sidekick.jar");
    if (GetFileAttributesW(java) == INVALID_FILE_ATTRIBUTES || GetFileAttributesW(jar) == INVALID_FILE_ATTRIBUTES)
        fail(L"Extract the entire ZIP first. Keep managersSidekick.exe, managers-sidekick.jar and the runtime folder together.");
    append(command, L"\""); append(command, java);
    append(command, L"\" \"-Dsidekick.dataDir="); append(command, directory);
    append(command, L"\\data\" -jar \""); append(command, jar); append(command, L"\"");
    STARTUPINFOW startup = {0}; startup.cb = sizeof(startup);
    PROCESS_INFORMATION process = {0};
    if (!CreateProcessW(java, command, NULL, NULL, FALSE, 0, NULL, directory, &startup, &process))
        fail(L"Could not start the bundled Java. Extract the complete package to a writable folder and try again.");
    CloseHandle(process.hThread);
    WaitForSingleObject(process.hProcess, INFINITE);
    DWORD status = 1; GetExitCodeProcess(process.hProcess, &status); CloseHandle(process.hProcess);
    if (status) fail(L"The application stopped unexpectedly. Check the package documentation and that its folder is writable.");
    ExitProcess(0);
}
