//from https://forum.unity.com/threads/any-way-to-tell-unity-to-generate-the-sln-file-via-script-or-command-line.392314/
using System.Collections.Generic;
using UnityEngine;
using UnityEditor;
using Unity.CodeEditor;
using System.Reflection;

// The recommended way to create the VisualStudio SLN from the command line is a call
// Unity.exe -executeMethod "UnityEditor.SyncVS.SyncSolution"
//
// Unfortunately, as of Unity 2021.3.21f1 the built-in UnityEditor.SyncVS.SyncSolution internally calls
// Unity.CodeEditor.CodeEditor.Editor.CurrentCodeEditor.SyncAll() where CurrentCodeEditor depends on the user preferences
// which may not actually be set to VS on a CI machine.
// (see https://github.com/Unity-Technologies/UnityCsReference/blob/master/Editor/Mono/CodeEditor/SyncVS.cs)
//
// This routine provides an re-implementation that avoids reliability on the preference setting
// Unity.exe -executeMethod "UnityEditor.SyncVS.SyncSolution"
namespace Wooga.UnityPlugin {
    public static class DefaultSolutionGenerator
    {
        public static void GenerateSolution()
        {
            // Ensure that the mono islands are up-to-date
            AssetDatabase.Refresh();

            List<IExternalCodeEditor> externalCodeEditors;

            // externalCodeEditors = Unity.CodeEditor.Editor.m_ExternalCodeEditors;
            // ... unfortunately this is private without any means of access. Use reflection to get the value ...
            externalCodeEditors = CodeEditor.Editor.GetType().GetField("m_ExternalCodeEditors", BindingFlags.NonPublic | BindingFlags.Instance).GetValue(CodeEditor.Editor) as List<IExternalCodeEditor>;

            foreach (var externalEditor in externalCodeEditors)
            {
              var typeName = externalEditor.GetType().Name;
              switch (typeName)
              {
                case "VisualStudioEditor":
                case "RiderScriptEditor":
                  Debug.Log($"Generating solution with {typeName}");
                  externalEditor.SyncAll();
                  return;
              }
            }
            Debug.LogError("no VisualStudioEditor (com.unity.ide.visualstudio) or RiderScriptEditor (com.unity.ide.rider) registered, can't generate solution");
        }
    }
}
