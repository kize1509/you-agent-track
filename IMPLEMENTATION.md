# Implementation Details

## Architecture Overview

This project implements an AI-powered workflow analysis agent using the Koog Agents framework. The agent analyzes YouTrack workflows to help users understand why their actions were rejected by workflow rules.

## Core Technologies

The implementation leverages the Koog DSL framework with the following concepts:

- AI Agents with custom system prompts
- Custom tools for external API integration
- Strategy-based execution graphs with nodes and edges
- OpenAI GPT-4 as the reasoning engine

## YouTrack Integration

The agent exposes three custom tools that communicate with the YouTrack REST API:

1. Issue Tool: Fetches detailed information for a specific issue
2. Workflows Tool: Retrieves all workflows associated with a project
3. Workflow Rules Tool: Fetches detailed rules for a specific workflow

Each tool wraps the YouTrack HTTP client and presents structured data to the agent.

## Agent Execution Flow

The agent follows a multi-step reasoning process:

1. User provides a query describing an issue and action, for example: "tried moving DEMO-4 into fixed state but got an error"

2. Agent extracts the issue ID from the query and calls the Issue Tool to fetch issue details

3. From the issue details, the agent identifies the project ID and calls the Workflows Tool to retrieve all project workflows

4. Agent analyzes workflow metadata and titles to identify relevant workflows

5. For each relevant workflow, the agent calls the Workflow Rules Tool to fetch detailed rule definitions

6. Agent performs final reasoning by matching the user action against workflow rules to identify the cause of rejection

7. Agent provides a detailed explanation including which rule caused the rejection and how to resolve it

## Strategy Implementation

The agent uses a custom strategy graph with the following nodes:

- LLM Request Node: Sends prompts to GPT-4
- Execute Tool Node: Executes tool calls requested by the LLM
- Send Tool Result Node: Returns tool results back to the LLM
- Start and Finish Nodes: Control flow boundaries

Edges define the execution flow based on message types, allowing the agent to iterate through multiple tool calls until reaching a conclusion.

## Design Principles

The project architecture follows SOLID principles:

- Single Responsibility: Each tool handles one specific API operation
- Open/Closed: Agent strategy is extensible without modifying core logic
- Liskov Substitution: HTTP service uses interface abstraction
- Interface Segregation: Separate interfaces for API and HTTP layers
- Dependency Inversion: Factory pattern for dependency injection

## Key Components

- Agent: WorkflowAnalysisAgent orchestrates the analysis process
- Tools: IssueTool, WorkflowsTool, WorkflowRulesTool provide API access
- Service Layer: YouTrackService encapsulates business logic
- API Client: YouTrackApiImpl handles HTTP communication
- Factory: DependencyFactory manages component initialization




## Strategy flowchart


                           START
                             │
                             ▼
                      ┌──────────────┐
                      │  LLM Request │
                      │  (nodeCallLLM)│
                      └──────────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
         [Assistant Message]       [Tool Call Requested]
                │                         │
                ▼                         ▼
            ┌────────┐            ┌──────────────┐
            │ FINISH │            │ Execute Tool │
            └────────┘            │(executeToolCall)│
                                  └──────────────┘
                                         │
                                         ▼
                                  ┌──────────────┐
                                  │ Send Tool    │
                                  │   Result     │
                                  │(sendToolResult)│
                                  └──────────────┘
                                         │
                        ┌────────────────┴────────────────┐
                        │                                 │
                 [Assistant Message]              [Tool Call Requested]
                        │                                 │
                        ▼                                 │
                    ┌────────┐                           │
                    │ FINISH │                           │
                    └────────┘                           │
                                                         │
                        └────────────────────────────────┘
                             (Loop back to Execute Tool)
