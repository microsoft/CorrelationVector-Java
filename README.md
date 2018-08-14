
# Correlation Vector

## Background

**Correlation Vector** (a.k.a. **cV**) is a  lightweight vector clock format and protocol standard for tracing and correlation of events through a distributed system based on a light weight vector clock.
The standard is widely used internally at Microsoft for first party applications and services and supported across multiple logging libraries and platforms (Services, Clients - Native, Managed, Js, iOS, Android etc). The standard powers a variety of different data processing needs ranging from distributed tracing & debugging to system and business intelligence, in various business organizations.

## Goals

- Make the standard specification externally avaiable for implementation by non-Microsoft components or Open Source Microsoft components
- Support wire interop scenarios b/w Microsoft-internal and external tracing systems
- Describe the benefits of vector based tracing formats and to provide an exemplar vector based format for future standardization efforts with similar protocols

## Resources

- Design Constraints and Scenarios for the Correlation Vector: [Link](Scenarios.md)
- Protocol Specification:
  - [v2.1](cV%20-%202.1.md)

## Future Roadmap

- v3.0
  - Support interop with W3C Distributed Tracing [Link](https://github.com/w3c/distributed-tracing)
  - Support for vector reset semantics to support traces of arbitrary depth
  - Support for versioning

# Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.


# General feedback and discussions?
Please start a discussion on the [Home repo issue tracker](https://github.com/Microsoft/CorrelationVector-Java/issues)
